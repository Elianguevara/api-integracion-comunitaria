package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostulationResponse {
    private Integer idPostulation;
    private String description; // Antes proposal
    private Double budget;
    private String providerName;
    private String providerImage;
    private String petitionTitle;
    private Integer petitionId;
    private String stateName;
    private Boolean isWinner;
    private String datePostulation;

    // Campo original por si acaso
    private String proposal;
}