package org.comunidad.api_integracion_comunitaria.controller;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.model.City;
import org.comunidad.api_integracion_comunitaria.model.Profession;
import org.comunidad.api_integracion_comunitaria.service.MetadataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService; // Ahora inyectamos el servicio

    @GetMapping("/professions")
    public ResponseEntity<List<Profession>> getAllProfessions() {
        return ResponseEntity.ok(metadataService.getAllProfessions());
    }

    @GetMapping("/cities")
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(metadataService.getAllCities());
    }
}