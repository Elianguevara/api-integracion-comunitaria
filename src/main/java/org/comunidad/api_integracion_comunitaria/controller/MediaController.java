package org.comunidad.api_integracion_comunitaria.controller;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.service.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final StorageService storageService;

    // Subir un archivo
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String filename = storageService.store(file);

        // Crear la URL pública para acceder a esa imagen
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(filename)
                .toUriString();

        Map<String, String> response = new HashMap<>();
        response.put("filename", filename);
        response.put("url", fileDownloadUri); // El Front guardará esta URL en la BD (ej: en user.profileImage)

        return ResponseEntity.ok(response);
    }

    // Endpoint auxiliar para descargar (opcional, ya que WebConfig lo maneja
    // estático)
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws IOException {
        Resource file = storageService.loadAsResource(filename);
        String contentType = Files.probeContentType(file.getFile().toPath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(file);
    }
}