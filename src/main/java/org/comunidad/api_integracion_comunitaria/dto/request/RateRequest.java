package org.comunidad.api_integracion_comunitaria.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RateRequest {

    @NotNull(message = "El ID del objetivo (Usuario a calificar) es obligatorio")
    private Integer targetId;

    // --- NUEVO CAMPO ---
    @NotNull(message = "El ID de la petición es obligatorio")
    private Integer petitionId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;
}