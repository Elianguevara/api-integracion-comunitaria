package org.comunidad.api_integracion_comunitaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.RateRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.ReviewResponse;
import org.comunidad.api_integracion_comunitaria.service.GradeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar las calificaciones.
 * Expone endpoints para crear reviews y consultar perfiles de reputación.
 */
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    /**
     * Endpoint para que un Cliente envíe una calificación a un Proveedor.
     *
     * @param request Cuerpo de la petición validado con {@link RateRequest}.
     * @return Mensaje de confirmación.
     */
    @PostMapping("/rate-provider")
    public ResponseEntity<String> rateProvider(@Valid @RequestBody RateRequest request) {
        gradeService.rateProvider(request);
        return ResponseEntity.ok("Calificación enviada con éxito.");
    }

    /**
     * Endpoint para que un Proveedor envíe una calificación a un Cliente.
     *
     * @param request Cuerpo de la petición validado con {@link RateRequest}.
     * @return Mensaje de confirmación.
     */
    @PostMapping("/rate-customer")
    public ResponseEntity<String> rateCustomer(@Valid @RequestBody RateRequest request) {
        gradeService.rateCustomer(request);
        return ResponseEntity.ok("Calificación enviada con éxito.");
    }

    /**
     * Obtiene las reseñas de un proveedor específico de manera paginada.
     * <p>
     * Ejemplo de uso desde Frontend:
     * {@code GET /api/grades/provider/123?page=0&size=5&sort=rating,desc}
     * </p>
     *
     * @param providerId ID del proveedor.
     * @param pageable   Configuración automática de paginación (Por defecto: 5
     *                   elementos, ordenado por ID descendente).
     * @return Página de reseñas.
     */
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<Page<ReviewResponse>> getProviderReviews(
            @PathVariable Integer providerId,
            @PageableDefault(page = 0, size = 5, sort = "idGradeProvider", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(gradeService.getProviderReviews(providerId, pageable));
    }

    /**
     * Verifica si el cliente autenticado ya ha calificado a este proveedor.
     */
    @GetMapping("/check-rated/{providerId}")
    public ResponseEntity<Boolean> checkIfRated(@PathVariable Integer providerId) {
        return ResponseEntity.ok(gradeService.hasCustomerRatedProvider(providerId));
    }
}