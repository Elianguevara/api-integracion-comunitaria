package org.comunidad.api_integracion_comunitaria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
// IMPORTACIONES NUEVAS PARA SOLUCIONAR EL WARNING DE PAGEIMPL
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web global para la aplicación.
 * <p>
 * Gestiona la exposición de recursos estáticos (imágenes subidas),
 * la configuración de políticas CORS para permitir la comunicación con el Frontend
 * y la correcta serialización de las respuestas paginadas.
 * </p>
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO) // <--- SOLUCIÓN AL WARNING DE PAGE
public class WebConfig implements WebMvcConfigurer {

    @Value("${storage.location}")
    private String storageLocation;

    /**
     * Configura el manejo de recursos estáticos.
     * Mapea las peticiones que lleguen a /uploads/** hacia la carpeta física definida en el storage.
     *
     * @param registry Registro de manejadores de recursos.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapea la URL "/uploads/archivo.png" a la carpeta física del disco configurada
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + storageLocation + "/");
    }

    /**
     * Configura las políticas de CORS (Cross-Origin Resource Sharing).
     * <p>
     * Esto es fundamental para permitir que el Frontend (React/Vite) pueda realizar
     * peticiones de tipo PATCH, PUT y DELETE al servidor sin ser bloqueado.
     * </p>
     *
     * @param registry Registro de configuraciones CORS.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todos los endpoints de la API
                .allowedOrigins("http://localhost:5173") // URL por defecto de Vite
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // Se añade PATCH para reactivaciones
                .allowedHeaders("*") // Permite todos los headers (Authorization, Content-Type, etc.)
                .allowCredentials(true) // Permite el envío de cookies o auth headers
                .maxAge(3600); // Cache de la configuración por 1 hora
    }
}