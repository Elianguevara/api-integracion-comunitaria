package org.comunidad.api_integracion_comunitaria.dto.request;

import lombok.Data;

@Data
public class UserProfileRequest {
    // Campos comunes (User)
    private String name;
    private String lastname;

    // Campos específicos de Customer
    private String phone;

    // Campos específicos de Provider
    private String description;
    private Integer idProfession; // Para cambiar de profesión
}