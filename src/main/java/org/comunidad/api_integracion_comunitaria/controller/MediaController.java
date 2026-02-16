package org.comunidad.api_integracion_comunitaria.controller;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        // Ahora store() nos devuelve directamente la URL final de Cloudinary
        String fileUrl = storageService.store(file);

        Map<String, String> response = new HashMap<>();
        response.put("url", fileUrl); // React recibirá esto y lo guardará

        return ResponseEntity.ok(response);
    }

    // El método @GetMapping("/{filename:.+}") se elimina porque el Frontend
    // leerá las imágenes directamente desde la URL de Cloudinary, liberando de carga a tu Backend.
}