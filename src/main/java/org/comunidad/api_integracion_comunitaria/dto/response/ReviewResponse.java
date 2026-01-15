package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Integer idReview;
    private String reviewerName; // Quién escribió la reseña
    private Integer rating;
    private String comment;
    private LocalDateTime date;
}