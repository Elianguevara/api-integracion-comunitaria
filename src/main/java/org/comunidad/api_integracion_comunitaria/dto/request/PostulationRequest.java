package org.comunidad.api_integracion_comunitaria.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostulationRequest {

    @NotNull(message = "El ID de la petición es obligatorio")
    private Integer idPetition;

    @NotBlank(message = "Debes incluir una descripción o mensaje")
    private String description;

    @NotNull(message = "El presupuesto es obligatorio")
    @Min(value = 1, message = "El presupuesto debe ser mayor a 0")
    private Double budget;
}