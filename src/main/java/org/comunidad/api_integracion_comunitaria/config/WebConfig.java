package org.comunidad.api_integracion_comunitaria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${storage.location}")
    private String storageLocation;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapea la URL "http://localhost:8080/uploads/foto.jpg" a la carpeta física del
        // disco
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + storageLocation + "/");
    }
}