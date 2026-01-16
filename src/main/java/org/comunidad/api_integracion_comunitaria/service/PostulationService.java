package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.PostulationRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PostulationResponse;
import org.comunidad.api_integracion_comunitaria.model.*;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Servicio encargado de gestionar las postulaciones de los proveedores a las
 * peticiones.
 * Incluye la lógica de creación, validación de reglas de negocio y adjudicación
 * de ganadores.
 */
@Service
@RequiredArgsConstructor
public class PostulationService {

        private final PostulationRepository postulationRepository;
        private final PetitionRepository petitionRepository;
        private final ProviderRepository providerRepository;
        private final UserRepository userRepository;
        private final NotificationService notificationService;
        private final PetitionStateRepository petitionStateRepository;
        private final PostulationStateRepository postulationStateRepository;

        /**
         * Permite a un proveedor postularse a una petición existente.
         */
        @Transactional
        public PostulationResponse createPostulation(PostulationRequest request) {
                // 1. Identificar al usuario logueado
                String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                                .getUsername();
                User user = userRepository.findByEmail(email).orElseThrow();

                // 2. Verificar que sea PROVEEDOR
                Provider provider = providerRepository.findByUser_IdUser(user.getIdUser())
                                .orElseThrow(() -> new RuntimeException(
                                                "Acceso denegado: Solo los proveedores registrados pueden postularse."));

                // 3. Buscar la petición
                Petition petition = petitionRepository.findById(request.getIdPetition())
                                .orElseThrow(() -> new RuntimeException("La petición solicitada no existe."));

                // --- VALIDACIONES DE NEGOCIO ---

                // A. Validar que la petición esté ABIERTA
                if (!"PUBLICADA".equalsIgnoreCase(petition.getState().getName())) {
                        throw new RuntimeException(
                                        "No puedes postularte: La petición no está disponible (Estado actual: "
                                                        + petition.getState().getName() + ").");
                }

                // B. Validar Fechas (Vencimiento)
                if (petition.getDateUntil() != null && LocalDate.now().isAfter(petition.getDateUntil())) {
                        throw new RuntimeException("El plazo para postularse ha vencido (Cerró el: "
                                        + petition.getDateUntil() + ").");
                }

                // C. Validar Anti-Auto-Postulación
                if (petition.getCustomer().getUser().getIdUser().equals(user.getIdUser())) {
                        throw new RuntimeException("No puedes postularte a tu propia petición.");
                }

                // 4. Validar duplicados
                if (postulationRepository.findByPetition_IdPetitionAndProvider_IdProvider(petition.getIdPetition(),
                                provider.getIdProvider()).isPresent()) {
                        throw new RuntimeException("Ya tienes una postulación activa para esta petición.");
                }

                // 5. Crear la postulación
                Postulation postulation = new Postulation();
                postulation.setPetition(petition);
                postulation.setProvider(provider);
                postulation.setProposal(request.getProposal());
                postulation.setWinner(false);
                postulation.setIsDeleted(false);

                PostulationState initialState = postulationStateRepository.findByName("ENVIADA")
                                .orElseThrow(() -> new RuntimeException(
                                                "Error de configuración: Estado 'ENVIADA' no existe en BD."));
                postulation.setState(initialState);

                postulationRepository.save(postulation);

                // Opcional: Notificar al Cliente
                // notificationService.sendNotification(petition.getCustomer().getUser(), "Nueva
                // Postulación", "...", "INFO", petition, postulation);

                return mapToResponse(postulation);
        }

        /**
         * Listar postulaciones por petición (Para el Cliente) - PAGINADO.
         * Permite ver los candidatos de 10 en 10.
         */
        public Page<PostulationResponse> getPostulationsByPetition(Integer idPetition, Pageable pageable) {
                return postulationRepository.findByPetition_IdPetition(idPetition, pageable)
                                .map(this::mapToResponse);
        }

        /**
         * Listar MIS postulaciones (Para el Proveedor) - PAGINADO.
         * Permite al proveedor ver su historial.
         */
        public Page<PostulationResponse> getMyPostulations(Pageable pageable) {
                String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                                .getUsername();
                User user = userRepository.findByEmail(email).orElseThrow();

                Provider provider = providerRepository.findByUser_IdUser(user.getIdUser())
                                .orElseThrow(() -> new RuntimeException("No tienes perfil de proveedor activo."));

                return postulationRepository.findByProvider_IdProvider(provider.getIdProvider(), pageable)
                                .map(this::mapToResponse);
        }

        /**
         * El Cliente elige a un ganador.
         */
        @Transactional
        public void acceptPostulation(Integer idPostulation) {
                // 1. Validaciones iniciales
                String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                                .getUsername();
                User clientUser = userRepository.findByEmail(email).orElseThrow();

                Postulation postulation = postulationRepository.findById(idPostulation)
                                .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));
                Petition petition = postulation.getPetition();

                if (!petition.getCustomer().getUser().getIdUser().equals(clientUser.getIdUser())) {
                        throw new RuntimeException(
                                        "No tienes permiso para aceptar esta postulación. No eres el dueño.");
                }

                if ("ADJUDICADA".equals(petition.getState().getName())
                                || "CERRADA".equals(petition.getState().getName())) {
                        throw new RuntimeException("Esta petición ya fue adjudicada o cerrada previamente.");
                }

                // 2. Marcar Ganadora
                PostulationState acceptedState = postulationStateRepository.findByName("ACEPTADA")
                                .orElseThrow(() -> new RuntimeException("Estado ACEPTADA no encontrado"));
                postulation.setState(acceptedState);
                postulation.setWinner(true);
                postulationRepository.save(postulation);

                // Notificar al ganador
                notificationService.sendNotification(
                                postulation.getProvider().getUser(),
                                "¡Felicidades! Tu propuesta fue aceptada",
                                "El cliente ha aceptado tu propuesta para: " + petition.getDescription(),
                                "SUCCESS",
                                petition,
                                postulation);

                // 3. Cerrar Petición
                PetitionState adjudicadaState = petitionStateRepository.findByName("ADJUDICADA")
                                .orElseThrow(() -> new RuntimeException("Estado ADJUDICADA no encontrado"));
                petition.setState(adjudicadaState);
                petitionRepository.save(petition);

                // 4. Rechazar automáticamente las demás
                PostulationState rejectedState = postulationStateRepository.findByName("RECHAZADA")
                                .orElseThrow(() -> new RuntimeException("Estado RECHAZADA no encontrado"));

                // TRUCO: Usamos Pageable.unpaged() para reutilizar el método del repositorio
                // pero trayendo TODAS para rechazarlas
                Page<Postulation> allCandidates = postulationRepository
                                .findByPetition_IdPetition(petition.getIdPetition(), Pageable.unpaged());

                for (Postulation p : allCandidates) {
                        if (!p.getIdPostulation().equals(idPostulation)) {
                                p.setState(rejectedState);
                                postulationRepository.save(p);

                                notificationService.sendNotification(
                                                p.getProvider().getUser(),
                                                "Postulación finalizada",
                                                "Otra propuesta fue seleccionada para la petición: "
                                                                + petition.getDescription(),
                                                "INFO",
                                                petition,
                                                p);
                        }
                }
        }

        private PostulationResponse mapToResponse(Postulation p) {
                return PostulationResponse.builder()
                                .idPostulation(p.getIdPostulation())
                                .proposal(p.getProposal())
                                .providerName(p.getProvider().getUser().getName() + " "
                                                + p.getProvider().getUser().getLastname())
                                .petitionTitle(p.getPetition().getDescription())
                                .state(p.getState().getName())
                                .isWinner(p.getWinner())
                                .build();
        }
}