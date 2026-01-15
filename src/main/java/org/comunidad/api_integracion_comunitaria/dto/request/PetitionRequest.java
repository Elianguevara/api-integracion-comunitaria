package org.comunidad.api_integracion_comunitaria.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PetitionRequest {

    @NotNull(message = "La descripción es obligatoria")
    @Size(min = 10, message = "La descripción debe tener al menos 10 caracteres")
    private String description;

    @NotNull(message = "Debes seleccionar un tipo de petición")
    private Integer idTypePetition;

    @NotNull(message = "Debes seleccionar una profesión requerida")
    private Integer idProfession;

    // --- NUEVO CAMPO ---
    @NotNull(message = "Debes seleccionar la ciudad donde se realizará el trabajo")
    private Integer idCity;

    @Future(message = "La fecha de cierre debe ser posterior a hoy")
    private LocalDate dateUntil;
}