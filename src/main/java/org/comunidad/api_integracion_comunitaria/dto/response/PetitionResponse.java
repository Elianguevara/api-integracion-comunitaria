package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class PetitionResponse {
    private Integer idPetition;
    private String description;

    // Aquí mapearemos el campo 'typePetitionName'
    private String typePetitionName;

    private String professionName;
    private String stateName;
    private LocalDate dateSince;
    private LocalDate dateUntil;
    private String customerName;
}