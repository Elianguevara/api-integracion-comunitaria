package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    // Opcional: Para errores de validación de formularios (ej: "email: formato
    // incorrecto")
    private Map<String, String> validationErrors;
}