package org.comunidad.api_integracion_comunitaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.PostulationRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PostulationResponse;
import org.comunidad.api_integracion_comunitaria.service.PostulationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/postulations")
@RequiredArgsConstructor
public class PostulationController {

    private final PostulationService postulationService;

    // Crear Postulación
    @PostMapping
    public ResponseEntity<PostulationResponse> create(@Valid @RequestBody PostulationRequest request) {
        return ResponseEntity.ok(postulationService.createPostulation(request));
    }

    /**
     * Ver candidatos de una petición (Para el Cliente Dueño).
     * Ejemplo Front: GET /api/postulations/petition/5?page=0&size=10
     */
    @GetMapping("/petition/{idPetition}")
    public ResponseEntity<Page<PostulationResponse>> getByPetition(
            @PathVariable Integer idPetition,
            @PageableDefault(page = 0, size = 10, sort = "idPostulation", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postulationService.getPostulationsByPetition(idPetition, pageable));
    }

    /**
     * Ver MIS postulaciones (Para el Proveedor Logueado).
     * Ejemplo Front: GET /api/postulations/me?page=0&size=10
     */
    @GetMapping("/me")
    public ResponseEntity<Page<PostulationResponse>> getMyPostulations(
            @PageableDefault(page = 0, size = 10, sort = "idPostulation", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postulationService.getMyPostulations(pageable));
    }

    // Aceptar ganador (Adjudicar)
    @PutMapping("/{idPostulation}/accept")
    public ResponseEntity<String> acceptPostulation(@PathVariable Integer idPostulation) {
        postulationService.acceptPostulation(idPostulation);
        return ResponseEntity.ok("Postulación aceptada y petición adjudicada correctamente.");
    }
}