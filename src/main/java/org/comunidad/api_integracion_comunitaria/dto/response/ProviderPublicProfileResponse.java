// src/main/java/org/comunidad/api_integracion_comunitaria/dto/response/ProviderPublicProfileResponse.java
package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ProviderPublicProfileResponse {
    private Integer idProvider;
    private Integer userId;
    private String name;
    private String lastname;
    private String profileImage;
    private String biography;
    private List<String> professions;
    private List<String> cities;
    private Double rating;
    private Integer totalReviews;
}