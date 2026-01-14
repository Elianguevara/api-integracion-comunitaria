package org.comunidad.api_integracion_comunitaria.model;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderCityId implements Serializable {
    // Los nombres deben coincidir con los campos de la entidad ProviderCity
    private Integer provider;
    private Integer city;
}