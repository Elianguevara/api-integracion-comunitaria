package org.comunidad.api_integracion_comunitaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.comunidad.api_integracion_comunitaria.dto.request.PetitionRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PetitionResponse;
import org.comunidad.api_integracion_comunitaria.service.PetitionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/petitions")
@RequiredArgsConstructor
public class PetitionController {

    private final PetitionService petitionService;

    // Crear una nueva petición (Requiere Token)
    @PostMapping
    public ResponseEntity<PetitionResponse> createPetition(
            @Valid @RequestBody PetitionRequest request) {
        return ResponseEntity.ok(petitionService.createPetition(request));
    }

    // Ver el feed de peticiones (Público o Privado, según tu SecurityConfig)
    @GetMapping("/feed")
    public ResponseEntity<List<PetitionResponse>> getFeed() {
        return ResponseEntity.ok(petitionService.getAllPublishedPetitions());
    }
}