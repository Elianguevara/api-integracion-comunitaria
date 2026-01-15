package org.comunidad.api_integracion_comunitaria.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RateRequest {

    @NotNull(message = "El ID del objetivo (Usuario a calificar) es obligatorio")
    private Integer targetId; // ID del Proveedor o Cliente según el caso

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating; // Estrellas de 1 a 5

    private String comment; // Opinión escrita
}