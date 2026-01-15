package org.comunidad.api_integracion_comunitaria.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    // Carpeta donde se guardarán las imágenes
    @Value("${storage.location}")
    private String storageLocation;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        // Al iniciar la app, crea la carpeta si no existe
        try {
            this.rootLocation = Paths.get(storageLocation);
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar la carpeta de almacenamiento", e);
        }
    }

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Error: Archivo vacío.");
            }

            // Generar nombre único: "foto.jpg" -> "uuid-foto.jpg"
            String filename = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

            // Copiar archivo al destino
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename))
                    .normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return filename; // Devolvemos el nombre para guardarlo en la BD
        } catch (IOException e) {
            throw new RuntimeException("Fallo al guardar archivo.", e);
        }
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("No se puede leer el archivo: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error leyendo archivo: " + filename, e);
        }
    }
}