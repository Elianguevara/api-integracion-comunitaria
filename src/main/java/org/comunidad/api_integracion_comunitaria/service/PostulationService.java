package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.PostulationRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PostulationResponse;
import org.comunidad.api_integracion_comunitaria.model.*;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostulationService {

    private final PostulationRepository postulationRepository;
    private final PetitionRepository petitionRepository;
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PetitionStateRepository petitionStateRepository;

    // CORRECCIÓN: Usamos el repositorio correcto para PostulationState
    private final PostulationStateRepository postulationStateRepository;

    @Transactional
    public PostulationResponse createPostulation(PostulationRequest request) {
        // 1. Identificar al usuario logueado
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUsername();
        User user = userRepository.findByEmail(email).orElseThrow();

        // 2. Verificar que sea PROVEEDOR
        Provider provider = providerRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new RuntimeException("Solo los proveedores pueden postularse."));

        // 3. Buscar la petición
        Petition petition = petitionRepository.findById(request.getIdPetition())
                .orElseThrow(() -> new RuntimeException("La petición no existe."));

        // 4. Validar: ¿Ya se postuló antes?
        if (postulationRepository
                .findByPetition_IdPetitionAndProvider_IdProvider(petition.getIdPetition(), provider.getIdProvider())
                .isPresent()) {
            throw new RuntimeException("Ya te has postulado a esta petición.");
        }

        // 5. Crear la postulación
        Postulation postulation = new Postulation();
        postulation.setPetition(petition);
        postulation.setProvider(provider);
        postulation.setProposal(request.getProposal());
        postulation.setWinner(false);
        postulation.setIsDeleted(false);

        // Estado inicial: "ENVIADA"
        // Ahora esto funciona porque postulationStateRepository devuelve un
        // PostulationState
        PostulationState initialState = postulationStateRepository.findByName("ENVIADA")
                .orElseThrow(() -> new RuntimeException("Estado 'ENVIADA' no configurado en la base de datos."));
        postulation.setState(initialState);

        postulationRepository.save(postulation);

        return mapToResponse(postulation);
    }

    // Listar postulaciones por petición (Para el Cliente)
    public List<PostulationResponse> getPostulationsByPetition(Integer idPetition) {
        return postulationRepository.findByPetition_IdPetition(idPetition).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PostulationResponse mapToResponse(Postulation p) {
        return PostulationResponse.builder()
                .idPostulation(p.getIdPostulation())
                .proposal(p.getProposal())
                .providerName(p.getProvider().getUser().getName() + " " + p.getProvider().getUser().getLastname())
                .petitionTitle(p.getPetition().getDescription())
                .state(p.getState().getName())
                .isWinner(p.getWinner())
                .build();
    }

    @Transactional
    public void acceptPostulation(Integer idPostulation) {
        // 1. Obtener usuario actual (el Cliente)
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUsername();
        User clientUser = userRepository.findByEmail(email).orElseThrow();

        // 2. Buscar la postulación
        Postulation postulation = postulationRepository.findById(idPostulation)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));

        Petition petition = postulation.getPetition();

        // 3. Validar que el usuario sea el DUEÑO de la petición
        if (!petition.getCustomer().getUser().getIdUser().equals(clientUser.getIdUser())) {
            throw new RuntimeException("No tienes permiso para aceptar esta postulación. No eres el dueño.");
        }

        // 4. Validar que la petición no esté ya cerrada
        if ("ADJUDICADA".equals(petition.getState().getName()) || "CERRADA".equals(petition.getState().getName())) {
            throw new RuntimeException("Esta petición ya fue adjudicada o cerrada.");
        }

        // --- LÓGICA DE ACTUALIZACIÓN ---

        // A. Marcar Postulación como Ganadora
        PostulationState acceptedState = postulationStateRepository.findByName("ACEPTADA")
                .orElseThrow(() -> new RuntimeException("Estado ACEPTADA no encontrado"));
        postulation.setState(acceptedState);
        postulation.setWinner(true);
        postulationRepository.save(postulation);
        // --- NOTIFICACIÓN AL GANADOR ---
        notificationService.sendNotification(
                postulation.getProvider().getUser(), // Usuario destinatario
                "¡Felicidades! Tu propuesta fue aceptada",
                "El cliente " + clientUser.getName() + " ha aceptado tu propuesta para: " + petition.getDescription(),
                "SUCCESS",
                petition,
                postulation);

        // B. Cambiar estado de la Petición a ADJUDICADA
        PetitionState adjudicadaState = petitionStateRepository.findByName("ADJUDICADA")
                .orElseThrow(() -> new RuntimeException("Estado ADJUDICADA no encontrado"));
        petition.setState(adjudicadaState);
        petitionRepository.save(petition);

        // C. Rechazar automáticamente las demás postulaciones de esa
        // petición
        PostulationState rejectedState = postulationStateRepository.findByName("RECHAZADA")
                .orElseThrow(() -> new RuntimeException("Estado RECHAZADA no encontrado"));

        List<Postulation> otherPostulations = postulationRepository.findByPetition_IdPetition(petition.getIdPetition());
        for (Postulation p : otherPostulations) {
            if (!p.getIdPostulation().equals(idPostulation)) { // Si no es la ganadora
                p.setState(rejectedState);
                postulationRepository.save(p);
                // --- NOTIFICACIÓN A LOS RECHAZADOS ---
                notificationService.sendNotification(
                        p.getProvider().getUser(),
                        "Postulación finalizada",
                        "Otra propuesta fue seleccionada para la petición: " + petition.getDescription(),
                        "INFO",
                        petition,
                        p);
            }
        }
    }
}