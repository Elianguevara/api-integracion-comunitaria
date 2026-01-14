package org.comunidad.api_integracion_comunitaria.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostulationRequest {
    @NotNull(message = "Debes indicar a qué petición te postulas")
    private Integer idPetition;

    @NotNull(message = "Debes escribir una propuesta")
    @Size(min = 20, message = "La propuesta debe ser detallada (mínimo 20 caracteres)")
    private String proposal;

    // Si manejas presupuesto, agrégalo aquí (ej: private Double amount;)
}