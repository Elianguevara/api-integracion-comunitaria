package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.comunidad.api_integracion_comunitaria.dto.request.PetitionRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PetitionResponse;
import org.comunidad.api_integracion_comunitaria.model.*;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio encargado de la gestión del ciclo de vida de las Peticiones (Trabajos solicitados).
 * Maneja la creación, consulta, visualización, eliminación lógica y transiciones de estado.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PetitionService {

        private final PetitionRepository petitionRepository;
        private final CustomerRepository customerRepository;
        private final UserRepository userRepository;
        private final ProfessionRepository professionRepository;
        private final TypePetitionRepository typePetitionRepository;
        private final PetitionStateRepository petitionStateRepository;
        private final NotificationService notificationService;
        private final CityRepository cityRepository;

        // --- NUEVO: Repositorio para guardar y buscar las fotos adjuntas ---
        private final PetitionAttachmentRepository petitionAttachmentRepository;

        /**
         * Crea una nueva petición de servicio en el sistema.
         *
         * @param email   Email del usuario autenticado.
         * @param request DTO con los datos del formulario de creación.
         * @return {@link PetitionResponse} con los datos de la petición creada.
         */
        @Transactional
        public PetitionResponse createPetition(String email, PetitionRequest request) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

                Customer customer = customerRepository.findByUser_IdUser(user.getIdUser())
                        .orElseThrow(() -> new RuntimeException("El usuario no tiene perfil de Cliente activo."));

                Petition petition = new Petition();
                petition.setCustomer(customer);
                petition.setDescription(request.getDescription());
                petition.setDateSince(LocalDate.now());
                petition.setDateUntil(request.getDateUntil());
                petition.setIsDeleted(false);

                petition.setProfession(professionRepository.findById(request.getIdProfession())
                        .orElseThrow(() -> new RuntimeException("Profesión no encontrada.")));

                petition.setTypePetition(typePetitionRepository.findById(request.getIdTypePetition())
                        .orElseThrow(() -> new RuntimeException("Tipo de petición no encontrado.")));

                City city = cityRepository.findById(request.getIdCity())
                        .orElseThrow(() -> new RuntimeException("Ciudad no encontrada."));
                petition.setCity(city);

                petition.setState(petitionStateRepository.findByName("PUBLICADA")
                        .orElseThrow(() -> new RuntimeException("Estado 'PUBLICADA' no configurado en BD.")));

                // 1. Guardamos la petición principal primero
                Petition savedPetition = petitionRepository.save(petition);

                // --- NUEVO: Guardamos la imagen si el frontend la envió ---
                if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
                        PetitionAttachment attachment = new PetitionAttachment();
                        attachment.setPetition(savedPetition);
                        attachment.setUrl(request.getImageUrl());

                        // Datos de auditoría obligatorios según tu modelo
                        attachment.setUserCreate(user);
                        attachment.setDateCreate(LocalDateTime.now());

                        petitionAttachmentRepository.save(attachment);
                }

                try {
                        notificationService.notifyProvidersByProfessionAndCity(
                                savedPetition.getProfession().getIdProfession(),
                                savedPetition.getCity().getIdCity(),
                                savedPetition);
                } catch (Exception e) {
                        log.error("Error enviando notificaciones para petición {}: {}", savedPetition.getIdPetition(), e.getMessage());
                }

                return mapToResponse(savedPetition);
        }

        /**
         * FEED PÚBLICO: Recupera peticiones en estado 'PUBLICADA', excluyendo las del usuario actual.
         */
        @Transactional(readOnly = true)
        public Page<PetitionResponse> getFeed(String email, Pageable pageable) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                return petitionRepository.findByState_NameAndCustomer_User_IdUserNot("PUBLICADA", user.getIdUser(), pageable)
                        .map(this::mapToResponse);
        }

        /**
         * MIS PETICIONES: Recupera el historial del usuario logueado.
         */
        @Transactional(readOnly = true)
        public Page<PetitionResponse> getMyPetitions(String email, Pageable pageable) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                Customer customer = customerRepository.findByUser_IdUser(user.getIdUser())
                        .orElseThrow(() -> new RuntimeException("No se encontró perfil de cliente para este usuario."));

                return petitionRepository.findByCustomer_IdCustomer(customer.getIdCustomer(), pageable)
                        .map(this::mapToResponse);
        }

        /**
         * Obtiene el detalle de una petición específica.
         */
        @Transactional(readOnly = true)
        public PetitionResponse getPetitionById(Long id, String email) {
                Petition petition = petitionRepository.findById(Math.toIntExact(id))
                        .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));

                return mapToResponse(petition);
        }

        /**
         * Finaliza una solicitud de trabajo (Estado 'FINALIZADA').
         */
        @Transactional
        public PetitionResponse completePetition(Long id, String userEmail) {
                Petition petition = findAndValidateOwnership(id, userEmail);

                PetitionState finishedState = petitionStateRepository.findByName("FINALIZADA")
                        .orElseThrow(() -> new RuntimeException("Estado 'FINALIZADA' no configurado en BD."));

                petition.setState(finishedState);
                log.info("Petición ID {} marcada como FINALIZADA por el usuario {}", id, userEmail);
                return mapToResponse(petitionRepository.save(petition));
        }

        /**
         * Cancela lógicamente una petición (Estado 'CANCELADA').
         */
        @Transactional
        public void deletePetition(Long id, String email) {
                Petition petition = findAndValidateOwnership(id, email);

                petition.setIsDeleted(true);
                petition.setState(petitionStateRepository.findByName("CANCELADA")
                        .orElseThrow(() -> new RuntimeException("Estado 'CANCELADA' no configurado en BD.")));

                petitionRepository.save(petition);
                log.info("Petición ID {} cancelada por el usuario {}", id, email);
        }

        /**
         * Reactiva una solicitud cancelada devolviéndola al estado 'PUBLICADA'.
         */
        @Transactional
        public PetitionResponse reactivatePetition(Long id, String userEmail) {
                Petition petition = findAndValidateOwnership(id, userEmail);

                if (petition.getDateUntil() != null && petition.getDateUntil().isBefore(LocalDate.now())) {
                        throw new RuntimeException("No se puede reactivar una solicitud con fecha de cierre vencida. Por favor, edita la fecha primero.");
                }

                PetitionState publicState = petitionStateRepository.findByName("PUBLICADA")
                        .orElseThrow(() -> new RuntimeException("Estado 'PUBLICADA' no configurado."));

                petition.setState(publicState);
                petition.setIsDeleted(false); // Restaurar visibilidad

                log.info("Petición ID {} reactivada con éxito por {}", id, userEmail);
                return mapToResponse(petitionRepository.save(petition));
        }

        /**
         * Método privado de utilidad para buscar una petición y validar que el usuario sea el dueño.
         */
        private Petition findAndValidateOwnership(Long id, String email) {
                Petition petition = petitionRepository.findById(Math.toIntExact(id))
                        .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

                if (!petition.getCustomer().getUser().getEmail().equals(email)) {
                        throw new RuntimeException("No tienes permisos sobre esta solicitud.");
                }
                return petition;
        }

        /**
         * Mapper para convertir Entidad a DTO.
         */
        private PetitionResponse mapToResponse(Petition petition) {
                String cityName = (petition.getCity() != null) ? petition.getCity().getName() : "Ubicación no especificada";

                // --- NUEVO: Construimos el Response usando el Builder y verificamos si hay fotos ---
                PetitionResponse.PetitionResponseBuilder responseBuilder = PetitionResponse.builder()
                        .idPetition(petition.getIdPetition())
                        .description(petition.getDescription())
                        .typePetitionName(petition.getTypePetition() != null ? petition.getTypePetition().getTypePetitionName() : "Sin categoría")
                        .professionName(petition.getProfession() != null ? petition.getProfession().getName() : "Profesión no especificada")
                        .stateName(petition.getState() != null ? petition.getState().getName() : "ESTADO_DESCONOCIDO")
                        .dateSince(petition.getDateSince())
                        .dateUntil(petition.getDateUntil())
                        .customerName(petition.getCustomer().getUser().getName() + " " + petition.getCustomer().getUser().getLastname())
                        .cityName(cityName);

                // Buscamos si tiene imágenes adjuntas en la base de datos
                List<PetitionAttachment> attachments = petitionAttachmentRepository.findByPetition_IdPetition(petition.getIdPetition());

                // Si hay fotos, sacamos la primera (en un futuro podrías enviar una lista si lo cambias a List<String>)
                if (!attachments.isEmpty()) {
                        responseBuilder.imageUrl(attachments.get(0).getUrl());
                }

                return responseBuilder.build();
        }
}