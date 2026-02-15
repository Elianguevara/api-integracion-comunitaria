package org.comunidad.api_integracion_comunitaria.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartConversationRequest {
    @NotNull
    private Integer petitionId;
    @NotNull
    private Integer providerId;
}