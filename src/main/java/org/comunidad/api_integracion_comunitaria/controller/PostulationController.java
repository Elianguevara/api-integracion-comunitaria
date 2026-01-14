package org.comunidad.api_integracion_comunitaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.PostulationRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PostulationResponse;
import org.comunidad.api_integracion_comunitaria.service.PostulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/postulations")
@RequiredArgsConstructor
public class PostulationController {

    private final PostulationService postulationService;

    @PostMapping
    public ResponseEntity<PostulationResponse> create(@Valid @RequestBody PostulationRequest request) {
        return ResponseEntity.ok(postulationService.createPostulation(request));
    }

    // Endpoint para que el dueño de la petición vea quién se postuló
    @GetMapping("/petition/{idPetition}")
    public ResponseEntity<List<PostulationResponse>> getByPetition(@PathVariable Integer idPetition) {
        return ResponseEntity.ok(postulationService.getPostulationsByPetition(idPetition));
    }

    @PutMapping("/{idPostulation}/accept")
    public ResponseEntity<String> acceptPostulation(@PathVariable Integer idPostulation) {
        postulationService.acceptPostulation(idPostulation);
        return ResponseEntity.ok("Postulación aceptada y petición adjudicada correctamente.");
    }
}