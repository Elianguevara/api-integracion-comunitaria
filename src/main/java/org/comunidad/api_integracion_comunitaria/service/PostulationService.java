package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.PostulationRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PostulationResponse;
import org.comunidad.api_integracion_comunitaria.model.*;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

        @Transactional
        public PostulationResponse createPostulation(String email, PostulationRequest request) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

                Provider provider = providerRepository.findByUser_IdUser(user.getIdUser())
                        .orElseThrow(() -> new RuntimeException("Acceso denegado: Solo proveedores."));

                Petition petition = petitionRepository.findById(request.getIdPetition())
                        .orElseThrow(() -> new RuntimeException("La petición no existe."));

                // Validaciones de negocio
                if (!"PUBLICADA".equalsIgnoreCase(petition.getState().getName())) {
                        throw new RuntimeException("La solicitud ya no acepta nuevas ofertas.");
                }

                if (petition.getDateUntil() != null && LocalDate.now().isAfter(petition.getDateUntil())) {
                        throw new RuntimeException("El plazo para postularse ha vencido.");
                }

                if (petition.getCustomer().getUser().getIdUser().equals(user.getIdUser())) {
                        throw new RuntimeException("No puedes ofertar en tu propia solicitud.");
                }

                if (postulationRepository.existsByPetition_IdPetitionAndProvider_IdProvider(petition.getIdPetition(), provider.getIdProvider())) {
                        throw new RuntimeException("Ya tienes una postulación activa para este trabajo.");
                }

                Postulation postulation = new Postulation();
                postulation.setPetition(petition);
                postulation.setProvider(provider);

                // Guardamos presupuesto y descripción combinados en el campo proposal
                String fullProposal = String.format("Presupuesto: $%s | Detalle: %s", request.getBudget(), request.getDescription());
                postulation.setProposal(fullProposal);

                postulation.setWinner(false);
                postulation.setIsDeleted(false);

                // Estado inicial de la postulación
                PostulationState initialState = postulationStateRepository.findByName("PENDIENTE")
                        .orElseThrow(() -> new RuntimeException("Error: Estado 'PENDIENTE' no encontrado en BD."));
                postulation.setState(initialState);

                postulationRepository.save(postulation);

                try {
                        notificationService.notifyNewPostulation(petition);
                } catch (Exception e) {
                        System.err.println("Error enviando notificación: " + e.getMessage());
                }

                return mapToResponse(postulation, request.getBudget());
        }

        /**
         * MÉTODO CORREGIDO: Recupera las postulaciones de una petición específica.
         * Es invocado por PostulationController.getByPetition.
         */
        @Transactional(readOnly = true)
        public List<PostulationResponse> getPostulationsByPetition(Integer idPetition, String email) {
                // Opcional: Validar que el email pertenezca al dueño de la petición antes de retornar
                return postulationRepository.findByPetition_IdPetition(idPetition).stream()
                        .map(this::mapToResponse)
                        .toList();
        }

        @Transactional
        public void acceptPostulation(Integer idPostulation, String clientEmail) {
                Postulation winner = postulationRepository.findById(idPostulation)
                        .orElseThrow(() -> new RuntimeException("Postulación no encontrada."));
                Petition petition = winner.getPetition();

                if (!petition.getCustomer().getUser().getEmail().equals(clientEmail)) {
                        throw new RuntimeException("No tienes permisos para realizar esta acción.");
                }

                // 1. Aceptar al Ganador
                PostulationState acceptedState = postulationStateRepository.findByName("ACEPTADA")
                        .orElseThrow(() -> new RuntimeException("Estado 'ACEPTADA' no configurado."));
                winner.setState(acceptedState);
                winner.setWinner(true);
                postulationRepository.save(winner);

                // 2. Actualizar la Petición a ADJUDICADA
                PetitionState adjudicadaState = petitionStateRepository.findByName("ADJUDICADA")
                        .orElseThrow(() -> new RuntimeException("Estado 'ADJUDICADA' no configurado."));
                petition.setState(adjudicadaState);
                petitionRepository.save(petition);

                // 3. Rechazar automáticamente al resto
                PostulationState rejectedState = postulationStateRepository.findByName("RECHAZADA")
                        .orElseThrow(() -> new RuntimeException("Estado 'RECHAZADA' no configurado."));

                List<Postulation> others = postulationRepository.findByPetition_IdPetitionAndIdPostulationNot(
                        petition.getIdPetition(), winner.getIdPostulation());

                for (Postulation loser : others) {
                        loser.setState(rejectedState);
                        postulationRepository.save(loser);
                }

                notificationService.notifyPostulationAccepted(winner);
        }

        @Transactional(readOnly = true)
        public Page<PostulationResponse> getMyPostulations(String email, Pageable pageable) {
                User user = userRepository.findByEmail(email).orElseThrow();
                Provider provider = providerRepository.findByUser_IdUser(user.getIdUser())
                        .orElseThrow(() -> new RuntimeException("Perfil de proveedor no encontrado."));

                return postulationRepository.findByProvider_IdProvider(provider.getIdProvider(), pageable)
                        .map(this::mapToResponse);
        }

        private PostulationResponse mapToResponse(Postulation p) {
                return PostulationResponse.builder()
                        .idPostulation(p.getIdPostulation())
                        .description(p.getProposal())
                        .providerName(p.getProvider().getUser().getName() + " " + p.getProvider().getUser().getLastname())
                        .providerImage(p.getProvider().getUser().getProfileImage())
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


        @Transactional(readOnly = true)
        public boolean checkIfApplied(Integer idPetition, String email) {
                User user = userRepository.findByEmail(email).orElseThrow();
                Provider provider = providerRepository.findByUser_IdUser(user.getIdUser())
                        .orElseThrow(() -> new RuntimeException("No eres proveedor."));

                return postulationRepository.existsByPetition_IdPetitionAndProvider_IdProvider(
                        idPetition, provider.getIdProvider());
        }
}