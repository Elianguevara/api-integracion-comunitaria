package org.comunidad.api_integracion_comunitaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.PetitionRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.PetitionResponse;
import org.comunidad.api_integracion_comunitaria.service.PetitionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/petitions")
@RequiredArgsConstructor
public class PetitionController {

    private final PetitionService petitionService;

    @PostMapping
    public ResponseEntity<PetitionResponse> createPetition(@Valid @RequestBody PetitionRequest request) {
        return ResponseEntity.ok(petitionService.createPetition(request));
    }

    // Feed General (Público, excluye propias)
    @GetMapping("/feed")
    public ResponseEntity<Page<PetitionResponse>> getFeed(
            @PageableDefault(page = 0, size = 10, sort = "dateSince", direction = Sort.Direction.DESC) Pageable pageable) {
        // Llamamos al método getFeed que actualizamos
        return ResponseEntity.ok(petitionService.getFeed(pageable));
    }

    // NUEVO: Historial Personal
    @GetMapping("/my")
    public ResponseEntity<Page<PetitionResponse>> getMyPetitions(
            @PageableDefault(page = 0, size = 10, sort = "dateSince", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(petitionService.getMyPetitions(pageable));
    }
}