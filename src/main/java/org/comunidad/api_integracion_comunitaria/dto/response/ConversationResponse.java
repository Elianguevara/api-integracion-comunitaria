package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ConversationResponse {
    private Long idConversation;
    private Integer petitionId;
    private String petitionTitle;
    private Integer otherParticipantId;
    private String otherParticipantName;
    private String otherParticipantRole;
    private String otherParticipantImage;
    private String lastMessage;
    private LocalDateTime updatedAt;
    private Long unreadCount;
    private Boolean isReadOnly; // <-- NUEVO CAMPO
}