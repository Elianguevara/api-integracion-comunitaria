package org.comunidad.api_integracion_comunitaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.ProviderProfileRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.ProviderPublicProfileResponse; // <-- NUEVO
import org.comunidad.api_integracion_comunitaria.service.ProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@Valid @RequestBody ProviderProfileRequest request) {
        providerService.updateProfile(request);
        return ResponseEntity.ok("Perfil de proveedor actualizado correctamente.");
    }

    // --- NUEVO ENDPOINT ---
    @GetMapping("/{id}")
    public ResponseEntity<ProviderPublicProfileResponse> getPublicProfile(@PathVariable Integer id) {
        return ResponseEntity.ok(providerService.getPublicProfile(id));
    }
}