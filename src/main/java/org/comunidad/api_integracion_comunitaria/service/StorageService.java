package org.comunidad.api_integracion_comunitaria.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final Cloudinary cloudinary;

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Error: Archivo vacío.");
            }

            // Subimos el archivo a Cloudinary
            // ObjectUtils.emptyMap() envía las opciones por defecto
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            // Cloudinary nos devuelve mucha info, pero solo nos interesa la URL segura
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Fallo al subir archivo a Cloudinary.", e);
        }
    }

    // Nota: Eliminamos los métodos init() y loadAsResource() porque
    // ya no guardaremos ni leeremos archivos del disco local del servidor.
}