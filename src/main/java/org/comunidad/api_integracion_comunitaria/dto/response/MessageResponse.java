package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {
    private Long idMessage;
    private String content;
    private LocalDateTime createdAt;
    private Integer senderId;
    private String senderName;
    private Boolean isMine; // Para que el frontend sepa si ponerlo a la derecha (verde) o izquierda (gris)
}