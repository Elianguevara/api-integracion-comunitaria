package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.Builder;
import lombok.Data;;

@Data
@Builder
public class PostulationResponse {
    private Integer idPostulation;
    private String proposal;
    private String providerName; // Nombre del proveedor
    private String petitionTitle; // Descripción breve de la petición
    private String state; // Estado de la postulación
    private boolean isWinner;
}