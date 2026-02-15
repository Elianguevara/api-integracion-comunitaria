package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.RateRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.ReviewResponse;
import org.comunidad.api_integracion_comunitaria.model.*;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio encargado de la gestión de Calificaciones y Reseñas (Reviews).
 * <p>
 * Maneja la lógica para que Clientes y Proveedores se califiquen mutuamente,
 * asegurando que exista una relación laboral previa (Postulación Ganadora)
 * vinculada a una petición específica antes de permitir la calificación.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class GradeService {

        private final GradeProviderRepository gradeProviderRepository;
        private final GradeCustomerRepository gradeCustomerRepository;
        private final UserRepository userRepository;
        private final ProviderRepository providerRepository;
        private final CustomerRepository customerRepository;
        private final PostulationRepository postulationRepository;

        // DEPENDENCIA: Para buscar la petición que se está calificando
        private final PetitionRepository petitionRepository;

        /**
         * Permite a un Cliente calificar a un Proveedor tras un trabajo completado.
         * <p>
         * Reglas de Negocio:
         * 1. El usuario autenticado debe tener perfil de Cliente.
         * 2. Debe existir una postulación aceptada (Winner=true) entre ambos para esa petición.
         * 3. La petición debe pertenecer al cliente que califica.
         * 4. No debe haberlo calificado previamente por ESTA petición.
         * </p>
         *
         * @param request DTO con la puntuación (1-5), comentario, ID del proveedor y ID de la petición.
         */
        @Transactional
        public void rateProvider(RateRequest request) {
                User user = getAuthenticatedUser();
                Customer customer = customerRepository.findByUser_IdUser(user.getIdUser())
                        .orElseThrow(() -> new RuntimeException("No tienes perfil de Cliente activo."));

                Provider provider = providerRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new RuntimeException("Proveedor no encontrado."));

                Petition petition = petitionRepository.findById(request.getPetitionId())
                        .orElseThrow(() -> new RuntimeException("Petición no encontrada."));

                // 1. VALIDACIÓN EXTRA: Verificar si ya lo calificó para ESTE trabajo específico
                if (gradeProviderRepository.existsByCustomer_IdCustomerAndProvider_IdProviderAndPetition_IdPetition(
                        customer.getIdCustomer(), provider.getIdProvider(), petition.getIdPetition())) {
                        throw new RuntimeException("Ya has calificado a este proveedor por este trabajo.");
                }

                // 2. VALIDACIÓN DE SEGURIDAD: ¿La petición le pertenece a este cliente?
                if (!petition.getCustomer().getIdCustomer().equals(customer.getIdCustomer())) {
                        throw new RuntimeException("Esta petición no te pertenece.");
                }

                // 3. VALIDACIÓN: ¿El proveedor ganó ESTA petición?
                boolean isWinner = postulationRepository.existsByPetition_IdPetitionAndProvider_IdProviderAndWinnerTrue(
                        petition.getIdPetition(), provider.getIdProvider());

                if (!isWinner) {
                        throw new RuntimeException("No puedes calificar a este proveedor porque no fue el adjudicado para esta petición.");
                }

                // Crear calificación
                GradeProvider review = new GradeProvider();
                review.setCustomer(customer);
                review.setProvider(provider);
                review.setPetition(petition); // ASIGNAMOS LA PETICIÓN
                review.setRating(request.getRating());
                review.setComment(request.getComment());
                review.setIsVisible(true);
                // Si la entidad tiene auditoría o fechas, puedes setearlo aquí

                gradeProviderRepository.save(review);
        }

        /**
         * Permite a un Proveedor calificar a un Cliente.
         */
        @Transactional
        public void rateCustomer(RateRequest request) {
                User user = getAuthenticatedUser();
                Provider provider = providerRepository.findByUser_IdUser(user.getIdUser())
                        .orElseThrow(() -> new RuntimeException("No tienes perfil de Proveedor activo."));

                Customer customer = customerRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new RuntimeException("Cliente no encontrado."));

                Petition petition = petitionRepository.findById(request.getPetitionId())
                        .orElseThrow(() -> new RuntimeException("Petición no encontrada."));

                // Validación anti-spam para este trabajo
                if (gradeCustomerRepository.existsByProvider_IdProviderAndCustomer_IdCustomerAndPetition_IdPetition(
                        provider.getIdProvider(), customer.getIdCustomer(), petition.getIdPetition())) {
                        throw new RuntimeException("Ya has calificado a este cliente por este trabajo.");
                }

                // Verificamos si este proveedor realmente ganó esta petición
                boolean isWinner = postulationRepository.existsByPetition_IdPetitionAndProvider_IdProviderAndWinnerTrue(
                        petition.getIdPetition(), provider.getIdProvider());

                if (!isWinner || !petition.getCustomer().getIdCustomer().equals(customer.getIdCustomer())) {
                        throw new RuntimeException("No estás autorizado a calificar a este cliente en este trabajo.");
                }

                GradeCustomer review = new GradeCustomer();
                review.setProvider(provider);
                review.setCustomer(customer);
                review.setPetition(petition); // ASIGNAMOS LA PETICIÓN
                review.setRating(request.getRating());
                review.setComment(request.getComment());
                review.setIsVisible(true);

                gradeCustomerRepository.save(review);
        }

        /**
         * Obtiene el listado de reseñas de un proveedor de forma paginada.
         */
        @Transactional(readOnly = true)
        public Page<ReviewResponse> getProviderReviews(Integer providerId, Pageable pageable) {
                return gradeProviderRepository.findByProvider_IdProvider(providerId, pageable)
                        .map(r -> ReviewResponse.builder()
                                .idReview(r.getIdGradeProvider())
                                .reviewerName(r.getCustomer().getUser().getName())
                                .rating(r.getRating())
                                .comment(r.getComment())
                                // CORRECCIÓN AQUÍ: Uso directo del objeto LocalDateTime para evitar parseos inseguros
                                .date(r.getDateCreate() != null ? r.getDateCreate() : LocalDateTime.now())
                                .build());
        }

        /**
         * Verifica si el cliente autenticado ya calificó a un proveedor específico para una petición específica.
         */
        @Transactional(readOnly = true)
        public boolean hasCustomerRatedProvider(Integer providerId, Integer petitionId) {
                User user = getAuthenticatedUser();
                return customerRepository.findByUser_IdUser(user.getIdUser())
                        .map(customer -> gradeProviderRepository.existsByCustomer_IdCustomerAndProvider_IdProviderAndPetition_IdPetition(
                                customer.getIdCustomer(), providerId, petitionId))
                        .orElse(false);
        }

        /**
         * Helper para obtener el usuario autenticado del contexto de seguridad.
         */
        private User getAuthenticatedUser() {
                String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                        .getUsername();
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Error de sesión: Usuario no encontrado."));
        }
}