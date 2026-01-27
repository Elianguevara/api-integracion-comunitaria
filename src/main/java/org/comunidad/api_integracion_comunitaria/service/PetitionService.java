package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.comunidad.api_integracion_comunitaria.dto.request.PetitionRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PetitionResponse;
import org.comunidad.api_integracion_comunitaria.model.City;
import org.comunidad.api_integracion_comunitaria.model.Customer;
import org.comunidad.api_integracion_comunitaria.model.Petition;
import org.comunidad.api_integracion_comunitaria.model.User;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Servicio encargado de la gestión del ciclo de vida de las Peticiones
 * (Trabajos solicitados).
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

        /**
         * Crea una nueva petición de servicio en el sistema.
         *
         * @param email Email del usuario autenticado.
         * @param request DTO con los datos del formulario de creación.
         * @return PetitionResponse DTO con los datos de la petición creada.
         */
        @Transactional
        public PetitionResponse createPetition(String email, PetitionRequest request) {
                // 1. Obtener usuario (Validación básica)
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

                // 2. Obtener perfil de cliente (Validación de Rol)
                Customer customer = customerRepository.findByUser_IdUser(user.getIdUser())
                        .orElseThrow(() -> new RuntimeException("El usuario no tiene perfil de Cliente activo."));

                // 3. Instanciar Entidad
                Petition petition = new Petition();
                petition.setCustomer(customer);
                petition.setDescription(request.getDescription());
                petition.setDateSince(LocalDate.now());
                petition.setDateUntil(request.getDateUntil());
                petition.setIsDeleted(false);

                // 4. Asignar y validar relaciones
                petition.setProfession(professionRepository.findById(request.getIdProfession())
                        .orElseThrow(() -> new RuntimeException("Profesión no encontrada.")));

                petition.setTypePetition(typePetitionRepository.findById(request.getIdTypePetition())
                        .orElseThrow(() -> new RuntimeException("Tipo de petición no encontrado.")));

                City city = cityRepository.findById(request.getIdCity())
                        .orElseThrow(() -> new RuntimeException("Ciudad no encontrada."));
                petition.setCity(city);

                // 5. Estado inicial
                petition.setState(petitionStateRepository.findByName("PUBLICADA")
                        .orElseThrow(() -> new RuntimeException("Estado 'PUBLICADA' no configurado en BD.")));

                // 6. Guardar
                Petition savedPetition = petitionRepository.save(petition);

                // 7. Notificar proveedores (Fail-safe: si falla, no rompe la creación)
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
         * FEED PÚBLICO:
         * Recupera peticiones 'PUBLICADA' excluyendo las creadas por el usuario que consulta.
         */
        @Transactional(readOnly = true)
        public Page<PetitionResponse> getFeed(String email, Pageable pageable) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                return petitionRepository.findByState_NameAndCustomer_User_IdUserNot("PUBLICADA", user.getIdUser(), pageable)
                        .map(this::mapToResponse);
        }

        /**
         * MIS PETICIONES:
         * Recupera el historial completo de peticiones del usuario logueado (como Cliente).
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
         * Mapper de Entidad a DTO.
         */
        private PetitionResponse mapToResponse(Petition petition) {
                String cityName = (petition.getCity() != null) ? petition.getCity().getName() : "Ubicación no especificada";

                return PetitionResponse.builder()
                        .idPetition(petition.getIdPetition())
                        .description(petition.getDescription())
                        .typePetitionName(petition.getTypePetition() != null ? petition.getTypePetition().getTypePetitionName() : "Sin categoría")
                        .professionName(petition.getProfession() != null ? petition.getProfession().getName() : "Profesión no especificada")
                        .stateName(petition.getState() != null ? petition.getState().getName() : "ESTADO_DESCONOCIDO")
                        .dateSince(petition.getDateSince())
                        .dateUntil(petition.getDateUntil())
                        .customerName(petition.getCustomer().getUser().getName() + " " + petition.getCustomer().getUser().getLastname())
                        .cityName(cityName)
                        .build();
        }
}