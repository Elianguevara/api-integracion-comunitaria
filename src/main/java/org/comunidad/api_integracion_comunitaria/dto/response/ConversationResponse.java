package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationResponse {
    private Long idConversation;
    private Integer petitionId;
    private String petitionTitle;
    private String otherUserName; // Nombre de la persona con la que hablo
    private String otherUserProfileImage; // URL de la foto (si la tienes implementada)
}