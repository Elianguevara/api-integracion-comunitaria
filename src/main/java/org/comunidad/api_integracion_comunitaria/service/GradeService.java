package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.RateRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.ReviewResponse;
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
public class GradeService {

    private final GradeProviderRepository gradeProviderRepository;
    private final GradeCustomerRepository gradeCustomerRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final CustomerRepository customerRepository;
    private final PostulationRepository postulationRepository; // Necesario para validar que trabajaron juntos

    // --- 1. Cliente califica a Proveedor ---
    @Transactional
    public void rateProvider(RateRequest request) {
        User user = getAuthenticatedUser();
        Customer customer = customerRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new RuntimeException("No tienes perfil de Cliente"));

        Provider provider = providerRepository.findById(request.getTargetId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        // VALIDACIÓN DE SEGURIDAD: ¿Realmente trabajaron juntos?
        // Buscamos si existe alguna postulación GANADORA entre este proveedor y una
        // petición de este cliente.
        boolean hasWorkedTogether = postulationRepository
                .existsByPetition_Customer_IdCustomerAndProvider_IdProviderAndWinnerTrue(
                        customer.getIdCustomer(), provider.getIdProvider());

        if (!hasWorkedTogether) {
            throw new RuntimeException(
                    "No puedes calificar a este proveedor porque no has completado ningún trabajo con él.");
        }

        // Crear calificación
        GradeProvider review = new GradeProvider();
        review.setCustomer(customer);
        review.setProvider(provider);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setIsVisible(true);
        // review.setGrade(...) // Si usaras el catálogo 'Grade', lo setearías aquí. Por
        // ahora usamos rating numérico.

        gradeProviderRepository.save(review);
    }

    // --- 2. Proveedor califica a Cliente ---
    @Transactional
    public void rateCustomer(RateRequest request) {
        User user = getAuthenticatedUser();
        Provider provider = providerRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new RuntimeException("No tienes perfil de Proveedor"));

        Customer customer = customerRepository.findById(request.getTargetId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // VALIDACIÓN: ¿Trabajaron juntos?
        boolean hasWorkedTogether = postulationRepository
                .existsByPetition_Customer_IdCustomerAndProvider_IdProviderAndWinnerTrue(
                        customer.getIdCustomer(), provider.getIdProvider());

        if (!hasWorkedTogether) {
            throw new RuntimeException("No puedes calificar a este cliente sin haberle realizado un trabajo.");
        }

        GradeCustomer review = new GradeCustomer();
        review.setProvider(provider);
        review.setCustomer(customer);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setIsVisible(true);

        gradeCustomerRepository.save(review);
    }

    // --- 3. Ver calificaciones de un Proveedor ---
    public List<ReviewResponse> getProviderReviews(Integer providerId) {
        return gradeProviderRepository.findByProvider_IdProvider(providerId).stream()
                .map(r -> ReviewResponse.builder()
                        .idReview(r.getIdGradeProvider())
                        .reviewerName(r.getCustomer().getUser().getName())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        // .date(r.getDateCreate()) // Asegúrate de que AuditableEntity tenga getters
                        // accesibles o usa LocalDateTime.now()
                        .build())
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser() {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUsername();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}