package org.comunidad.api_integracion_comunitaria.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ProviderProfileRequest {

    @NotNull(message = "La profesión es obligatoria")
    private Integer idProfession;

    private String description;

    @NotNull(message = "Debes seleccionar al menos una ciudad")
    private List<Integer> cityIds; // Lista de IDs de las ciudades donde trabaja
}