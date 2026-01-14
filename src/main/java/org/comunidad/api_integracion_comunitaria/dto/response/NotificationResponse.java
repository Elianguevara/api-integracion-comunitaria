package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Integer id;
    private String title;
    private String message;
    private String type; // Ej: "INFO", "SUCCESS", "WARNING"
    private boolean isRead;
    private LocalDateTime createdAt;
    private Integer relatedPostulationId;
    private Integer relatedPetitionId;
}