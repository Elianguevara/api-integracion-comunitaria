// api-integracion-comunitaria/src/main/java/org/comunidad/api_integracion_comunitaria/dto/response/PostulationResponse.java
package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostulationResponse {
    private Integer idPostulation;
    private String description;
    private Double budget;

    // AGREGA ESTA LÍNEA 👇
    private Integer providerId;

    private String providerName;
    private String providerImage;
    private Double providerRating;
    private String petitionTitle;
    private Integer petitionId;
    private String stateName;
    private Boolean isWinner;
    private String datePostulation;
    private String proposal;
}