package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.comunidad.api_integracion_comunitaria.dto.request.PostulationRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PostulationResponse;
import org.comunidad.api_integracion_comunitaria.model.*;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio encargado de gestionar las postulaciones de los proveedores a las peticiones de trabajo.
 * <p>
 * Maneja el ciclo de vida de una oferta, desde su creación hasta la adjudicación
 * del trabajo por parte del cliente.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostulationService {

        private final PostulationRepository postulationRepository;
        private final PetitionRepository petitionRepository;
        private final ProviderRepository providerRepository;
        private final UserRepository userRepository;
        private final NotificationService notificationService;
        private final PetitionStateRepository petitionStateRepository;
        private final PostulationStateRepository postulationStateRepository;
        private final GradeProviderRepository gradeProviderRepository; // Repositorio para obtener el promedio

        /**
         * Crea una nueva postulación de un proveedor para una petición específica.
         *
         * @param email   Email del proveedor autenticado.
         * @param request Datos de la postulación (presupuesto y descripción).
         * @return {@link PostulationResponse} con los datos de la oferta enviada.
         */
        @Transactional
        public PostulationResponse createPostulation(String email, PostulationRequest request) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

                Provider provider = providerRepository.findByUser_IdUser(user.getIdUser())
                        .orElseThrow(() -> new RuntimeException("Acceso denegado: Solo proveedores pueden postularse."));

                Petition petition = petitionRepository.findById(request.getIdPetition())
                        .orElseThrow(() -> new RuntimeException("La petición no existe."));

                // REGLA DE ORO: Solo permitir si está PUBLICADA
                if (!"PUBLICADA".equalsIgnoreCase(petition.getState().getName())) {
                        throw new RuntimeException("No puedes postularte a una solicitud que está " + petition.getState().getName());
                }

                // Evitar duplicados
                if (postulationRepository.existsByPetition_IdPetitionAndProvider_IdProvider(petition.getIdPetition(), provider.getIdProvider())) {
                        throw new RuntimeException("Ya te has postulado a este trabajo.");
                }

                Postulation postulation = new Postulation();
                postulation.setPetition(petition);
                postulation.setProvider(provider);

                // Formatear la propuesta para que guarde budget y descripción
                postulation.setProposal(String.format("Presupuesto: $%s | Detalle: %s", request.getBudget(), request.getDescription()));
                postulation.setWinner(false);
                postulation.setIsDeleted(false);

                // Estado inicial
                PostulationState initialState = postulationStateRepository.findByName("PENDIENTE")
                        .orElseThrow(() -> new RuntimeException("Error interno: Estado PENDIENTE no configurado."));
                postulation.setState(initialState);

                return mapToResponse(postulationRepository.save(postulation), request.getBudget());
        }

        /**
         * Recupera todas las postulaciones asociadas a una petición.
         *
         * @param idPetition ID de la petición.
         * @param email      Email del usuario que consulta.
         * @return Lista de postulaciones.
         */
        @Transactional(readOnly = true)
        public List<PostulationResponse> getPostulationsByPetition(Integer idPetition, String email) {
                return postulationRepository.findByPetition_IdPetition(idPetition).stream()
                        .map(this::mapToResponse)
                        .toList();
        }

        /**
         * Acepta una postulación y adjudica el trabajo al proveedor correspondiente.
         * <p>
         * Se han añadido validaciones para evitar adjudicar en peticiones canceladas o finalizadas.
         * </p>
         *
         * @param idPostulation ID de la postulación ganadora.
         * @param clientEmail   Email del cliente que adjudica (debe ser el dueño).
         */
        @Transactional
        public void acceptPostulation(Integer idPostulation, String clientEmail) {
                Postulation winner = postulationRepository.findById(idPostulation)
                        .orElseThrow(() -> new RuntimeException("Postulación no encontrada."));

                Petition petition = winner.getPetition();

                // 1. Validar Propiedad
                if (!petition.getCustomer().getUser().getEmail().equals(clientEmail)) {
                        throw new RuntimeException("No tienes permisos para realizar esta acción.");
                }

                // 2. CORRECCIÓN: Validar que la petición no esté CANCELADA o FINALIZADA
                String currentState = petition.getState().getName();
                if ("CANCELADA".equalsIgnoreCase(currentState) || "FINALIZADA".equalsIgnoreCase(currentState)) {
                        throw new RuntimeException("No se puede adjudicar una solicitud que está " + currentState);
                }

                // 3. Aceptar al Ganador
                PostulationState acceptedState = postulationStateRepository.findByName("ACEPTADA")
                        .orElseThrow(() -> new RuntimeException("Estado 'ACEPTADA' no configurado."));
                winner.setState(acceptedState);
                winner.setWinner(true);
                postulationRepository.save(winner);

                // 4. Actualizar la Petición a ADJUDICADA
                PetitionState adjudicadaState = petitionStateRepository.findByName("ADJUDICADA")
                        .orElseThrow(() -> new RuntimeException("Estado 'ADJUDICADA' no configurado."));
                petition.setState(adjudicadaState);
                petitionRepository.save(petition);

                // 5. Rechazar automáticamente al resto
                PostulationState rejectedState = postulationStateRepository.findByName("RECHAZADA")
                        .orElseThrow(() -> new RuntimeException("Estado 'RECHAZADA' no configurado."));

                List<Postulation> others = postulationRepository.findByPetition_IdPetitionAndIdPostulationNot(
                        petition.getIdPetition(), winner.getIdPostulation());

                for (Postulation loser : others) {
                        loser.setState(rejectedState);
                        postulationRepository.save(loser);
                }

                notificationService.notifyPostulationAccepted(winner);
                log.info("Petición ID {} adjudicada al proveedor ID {}", petition.getIdPetition(), winner.getProvider().getIdProvider());
        }

        /**
         * Obtiene las postulaciones realizadas por el proveedor autenticado.
         */
        @Transactional(readOnly = true)
        public Page<PostulationResponse> getMyPostulations(String email, Pageable pageable) {
                User user = userRepository.findByEmail(email).orElseThrow();
                Provider provider = providerRepository.findByUser_IdUser(user.getIdUser())
                        .orElseThrow(() -> new RuntimeException("Perfil de proveedor no encontrado."));

                return postulationRepository.findByProvider_IdProvider(provider.getIdProvider(), pageable)
                        .map(this::mapToResponse);
        }

        /**
         * Comprueba si un proveedor ya se postuló a una petición.
         */
        @Transactional(readOnly = true)
        public boolean checkIfApplied(Integer idPetition, String email) {
                User user = userRepository.findByEmail(email).orElseThrow();
                Provider provider = providerRepository.findByUser_IdUser(user.getIdUser())
                        .orElseThrow(() -> new RuntimeException("No eres proveedor."));

                return postulationRepository.existsByPetition_IdPetitionAndProvider_IdProvider(
                        idPetition, provider.getIdProvider());
        }

        private PostulationResponse mapToResponse(Postulation p) {
                // 1. Buscamos el promedio en la base de datos
                Double avg = gradeProviderRepository.getAverageRating(p.getProvider().getIdProvider());

                // 2. Si es null (no tiene reseñas), le ponemos 0.0. Si tiene, lo redondeamos a 1 decimal
                Double finalRating = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;

                return PostulationResponse.builder()
                        .idPostulation(p.getIdPostulation())
                        .description(p.getProposal())
                        .providerId(p.getProvider().getIdProvider())
                        .providerName(p.getProvider().getUser().getName() + " " + p.getProvider().getUser().getLastname())
                        .providerImage(p.getProvider().getUser().getProfileImage())
                        .providerRating(finalRating) // <--- ¡AQUÍ GUARDAMOS EL PROMEDIO!
                        .petitionTitle(p.getPetition().getDescription())
                        .petitionId(p.getPetition().getIdPetition())
                        .stateName(p.getState().getName())
                        .isWinner(p.getWinner())
                        .datePostulation(p.getDateCreate() != null ? p.getDateCreate().toString() : "")
                        .build();
        }

        private PostulationResponse mapToResponse(Postulation p, Double originalBudget) {
                PostulationResponse response = mapToResponse(p);
                response.setBudget(originalBudget);
                return response;
        }
}