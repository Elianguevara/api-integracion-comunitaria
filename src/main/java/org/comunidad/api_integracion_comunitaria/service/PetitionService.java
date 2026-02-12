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
import java.util.Objects;

/**
 * Servicio encargado de la gestión del ciclo de vida de las Peticiones (Trabajos solicitados).
 * Maneja la creación, consulta, visualización y eliminación lógica de las solicitudes.
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
         * @param email   Email del usuario autenticado (extraído del token JWT).
         * @param request DTO con los datos del formulario de creación.
         * @return PetitionResponse DTO con los datos de la petición creada.
         * @throws RuntimeException Si el usuario no existe, no es Cliente o faltan datos maestros.
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
         * Recupera peticiones en estado 'PUBLICADA', excluyendo las creadas por el usuario que consulta.
         * Ideal para que los proveedores busquen trabajo.
         *
         * @param email    Email del usuario que consulta (para excluir sus propias peticiones).
         * @param pageable Configuración de paginación.
         * @return Página de PetitionResponse.
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
         * Recupera el historial completo de peticiones del usuario logueado (rol Cliente).
         * Incluye peticiones activas, finalizadas o canceladas.
         *
         * @param email    Email del usuario autenticado.
         * @param pageable Configuración de paginación.
         * @return Página de PetitionResponse.
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
         * Obtiene el detalle de una petición específica por su ID.
         *
         * @param id    ID de la petición.
         * @param email Email del usuario que consulta (para validaciones futuras o auditoría).
         * @return PetitionResponse DTO con el detalle.
         * @throws RuntimeException Si la petición no existe.
         */
        @Transactional(readOnly = true)
        public PetitionResponse getPetitionById(Long id, String email) {
                Petition petition = petitionRepository.findById(Math.toIntExact(id))
                        .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));

                // Aquí podrías agregar lógica extra: ¿Es una petición privada? ¿El usuario tiene permiso?
                // Por ahora, asumimos que si existe, se puede ver.
                return mapToResponse(petition);
        }

        /**
         * Elimina (lógicamente) una petición del sistema.
         * Solo el dueño de la petición (el Cliente que la creó) puede eliminarla.
         * Cambia el estado a 'CANCELADA' y marca isDeleted = true.
         *
         * @param id    ID de la petición a eliminar.
         * @param email Email del usuario que intenta eliminar.
         * @throws RuntimeException Si la petición no existe, el usuario no existe, o no es el dueño.
         */
        @Transactional
        public void deletePetition(Long id, String email) {
                Petition petition = petitionRepository.findById(Math.toIntExact(id))
                        .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                // Validación de Seguridad: Solo el dueño puede borrarla
                if (!Objects.equals(petition.getCustomer().getUser().getIdUser(), user.getIdUser())) {
                        throw new RuntimeException("No tienes permiso para eliminar esta solicitud. No eres el propietario.");
                }

                // Borrado Lógico (Soft Delete)
                petition.setIsDeleted(true);
                petition.setState(petitionStateRepository.findByName("CANCELADA")
                        .orElseThrow(() -> new RuntimeException("Estado 'CANCELADA' no configurado en BD.")));

                petitionRepository.save(petition);
                log.info("Petición ID {} eliminada lógicamente por el usuario {}", id, email);
        }

        /**
         * Mapper auxiliar para convertir Entidad Petition a DTO PetitionResponse.
         *
         * @param petition Entidad de base de datos.
         * @return DTO para el frontend.
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