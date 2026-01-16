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

/**
 * Servicio encargado de la gestión de Calificaciones y Reseñas (Reviews).
 * <p>
 * Maneja la lógica para que Clientes y Proveedores se califiquen mutuamente,
 * asegurando que exista una relación laboral previa (Postulación Ganadora)
 * antes de permitir la calificación.
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

        /**
         * Permite a un Cliente calificar a un Proveedor tras un trabajo completado.
         * <p>
         * Reglas de Negocio:
         * 1. El usuario autenticado debe tener perfil de Cliente.
         * 2. Debe existir una postulación aceptada (Winner=true) entre ambos.
         * </p>
         *
         * @param request DTO con la puntuación (1-5), comentario e ID del proveedor.
         * @throws RuntimeException Si no son el cliente, no existe el proveedor o no
         *                          han trabajado juntos.
         */
        @Transactional
        public void rateProvider(RateRequest request) {
                User user = getAuthenticatedUser();
                Customer customer = customerRepository.findByUser_IdUser(user.getIdUser())
                                .orElseThrow(() -> new RuntimeException("No tienes perfil de Cliente activo."));

                Provider provider = providerRepository.findById(request.getTargetId())
                                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado."));

                // VALIDACIÓN DE SEGURIDAD: Verificar historial laboral
                boolean hasWorkedTogether = postulationRepository
                                .existsByPetition_Customer_IdCustomerAndProvider_IdProviderAndWinnerTrue(
                                                customer.getIdCustomer(), provider.getIdProvider());

                if (!hasWorkedTogether) {
                        throw new RuntimeException(
                                        "No puedes calificar a este proveedor porque no has completado ningún trabajo adjudicado con él.");
                }

                // Crear calificación
                GradeProvider review = new GradeProvider();
                review.setCustomer(customer);
                review.setProvider(provider);
                review.setRating(request.getRating());
                review.setComment(request.getComment());
                review.setIsVisible(true);
                // Aquí podrías agregar fecha de creación si tu entidad lo soporta
                // explícitamente

                gradeProviderRepository.save(review);
        }

        /**
         * Permite a un Proveedor calificar a un Cliente.
         * <p>
         * Es la contraparte del método {@link #rateProvider}, asegurando reciprocidad
         * en el sistema de reputación.
         * </p>
         *
         * @param request DTO con la puntuación, comentario e ID del cliente.
         */
        @Transactional
        public void rateCustomer(RateRequest request) {
                User user = getAuthenticatedUser();
                Provider provider = providerRepository.findByUser_IdUser(user.getIdUser())
                                .orElseThrow(() -> new RuntimeException("No tienes perfil de Proveedor activo."));

                Customer customer = customerRepository.findById(request.getTargetId())
                                .orElseThrow(() -> new RuntimeException("Cliente no encontrado."));

                // VALIDACIÓN: ¿Trabajaron juntos?
                boolean hasWorkedTogether = postulationRepository
                                .existsByPetition_Customer_IdCustomerAndProvider_IdProviderAndWinnerTrue(
                                                customer.getIdCustomer(), provider.getIdProvider());

                if (!hasWorkedTogether) {
                        throw new RuntimeException(
                                        "No puedes calificar a este cliente sin haberle realizado un trabajo adjudicado.");
                }

                GradeCustomer review = new GradeCustomer();
                review.setProvider(provider);
                review.setCustomer(customer);
                review.setRating(request.getRating());
                review.setComment(request.getComment());
                review.setIsVisible(true);

                gradeCustomerRepository.save(review);
        }

        /**
         * Obtiene el listado de reseñas de un proveedor de forma paginada.
         * <p>
         * Ideal para mostrar en el perfil público del proveedor sin sobrecargar la
         * vista.
         * </p>
         *
         * @param providerId ID del proveedor a consultar.
         * @param pageable   Configuración de la página (tamaño, número de página).
         * @return Una página (Page) de objetos {@link ReviewResponse}.
         */
        public Page<ReviewResponse> getProviderReviews(Integer providerId, Pageable pageable) {
                return gradeProviderRepository.findByProvider_IdProvider(providerId, pageable)
                                .map(r -> ReviewResponse.builder()
                                                .idReview(r.getIdGradeProvider())
                                                // Solo mostramos el nombre de pila por privacidad básica
                                                .reviewerName(r.getCustomer().getUser().getName())
                                                .rating(r.getRating())
                                                .comment(r.getComment())
                                                // .date(r.getDateCreate()) // Descomentar si la entidad tiene fecha
                                                // accesible
                                                .build());
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