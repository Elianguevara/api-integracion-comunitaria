package org.comunidad.api_integracion_comunitaria.dto.request;

import lombok.Data;

@Data
public class UserProfileRequest {
    private String name;
    private String lastname;
    private String phone;
    private String description;
    private Integer idProfession;
    private String profileImage; // <--- AGREGAR ESTO
}