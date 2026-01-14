package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;

import org.comunidad.api_integracion_comunitaria.dto.request.PetitionRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PetitionResponse;
import org.comunidad.api_integracion_comunitaria.model.Customer;
import org.comunidad.api_integracion_comunitaria.model.Petition;
import org.comunidad.api_integracion_comunitaria.model.User;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetitionService {

    private final PetitionRepository petitionRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProfessionRepository professionRepository;
    private final TypePetitionRepository typePetitionRepository;
    private final PetitionStateRepository petitionStateRepository;
    private final NotificationService notificationService;

    @Transactional
    public PetitionResponse createPetition(PetitionRequest request) {
        // 1. Obtener usuario autenticado
        String userEmail = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUsername();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Obtener perfil de cliente (Si no existe, deberíamos crearlo o lanzar
        // error)
        // NOTA: Para probar rápido, asegúrate de que el usuario tenga un registro en
        // 'n_customer'
        Customer customer = customerRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new RuntimeException("El usuario no tiene perfil de Cliente"));

        // 3. Crear Entidad
        Petition petition = new Petition();
        petition.setCustomer(customer);
        petition.setDescription(request.getDescription());
        petition.setDateSince(LocalDate.now());
        petition.setDateUntil(request.getDateUntil());
        petition.setIsDeleted(false);

        // 4. Asignar relaciones
        petition.setProfession(professionRepository.findById(request.getIdProfession())
                .orElseThrow(() -> new RuntimeException("Profesión no encontrada")));

        petition.setTypePetition(typePetitionRepository.findById(request.getIdTypePetition())
                .orElseThrow(() -> new RuntimeException("Tipo de petición no encontrado")));

        // Asignar estado inicial 'PUBLICADA'
        petition.setState(petitionStateRepository.findByName("PUBLICADA")
                .orElseThrow(() -> new RuntimeException("Estado 'PUBLICADA' no configurado en BD")));

        Petition savedPetition = petitionRepository.save(petition);

        // --- NUEVO: Notificar a los proveedores ---
        // Lo hacemos en un try-catch para que si falla la notificación,
        // NO falle la creación de la petición (la notificación es secundaria)
        try {
            notificationService.notifyProvidersByProfession(
                    savedPetition.getProfession().getIdProfession(),
                    savedPetition);
        } catch (Exception e) {
            System.err.println("Error enviando notificaciones: " + e.getMessage());
        }

        return mapToResponse(savedPetition);

    }

    public List<PetitionResponse> getAllPublishedPetitions() {
        return petitionRepository.findByState_Name("PUBLICADA").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PetitionResponse mapToResponse(Petition petition) {
        return PetitionResponse.builder()
                .idPetition(petition.getIdPetition())
                .description(petition.getDescription())
                // CORRECCIÓN: Usamos getTypePetitionName() en lugar de getName()
                .typePetitionName(petition.getTypePetition().getTypePetitionName())
                .professionName(petition.getProfession().getName()) // Verifica que en Profession sea 'getName()'
                .stateName(petition.getState().getName())
                .dateSince(petition.getDateSince())
                .dateUntil(petition.getDateUntil())
                .customerName(petition.getCustomer().getUser().getName() + " "
                        + petition.getCustomer().getUser().getLastname())
                .build();
    }
}