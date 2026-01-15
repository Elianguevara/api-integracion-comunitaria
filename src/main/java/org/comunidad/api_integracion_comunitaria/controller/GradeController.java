package org.comunidad.api_integracion_comunitaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.RateRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.ReviewResponse;
import org.comunidad.api_integracion_comunitaria.service.GradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    // Cliente califica -> Proveedor
    @PostMapping("/rate-provider")
    public ResponseEntity<String> rateProvider(@Valid @RequestBody RateRequest request) {
        gradeService.rateProvider(request);
        return ResponseEntity.ok("Calificación enviada con éxito.");
    }

    // Proveedor califica -> Cliente
    @PostMapping("/rate-customer")
    public ResponseEntity<String> rateCustomer(@Valid @RequestBody RateRequest request) {
        gradeService.rateCustomer(request);
        return ResponseEntity.ok("Calificación enviada con éxito.");
    }

    // Ver perfil público (reviews) del proveedor
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ReviewResponse>> getProviderReviews(@PathVariable Integer providerId) {
        return ResponseEntity.ok(gradeService.getProviderReviews(providerId));
    }
}