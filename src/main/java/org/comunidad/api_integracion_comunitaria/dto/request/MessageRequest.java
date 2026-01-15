package org.comunidad.api_integracion_comunitaria.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank(message = "El contenido no puede estar vacío")
    private String content;
}