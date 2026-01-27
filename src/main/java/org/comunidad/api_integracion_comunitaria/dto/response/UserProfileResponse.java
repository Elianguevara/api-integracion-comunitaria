package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class UserProfileResponse {
    private Integer id;
    private String name;
    private String lastname;
    private String email;
    private String role; // "CUSTOMER" o "PROVIDER"
    private String profileImage;

    // Campos específicos (pueden ser null dependiendo del rol)
    private String phone;       // Viene de Customer
    private String address;     // Viene de Address (concatenado o objeto)
    private String description; // Viene de Provider
    private String profession;  // Viene de Provider -> Profession

    // Estadísticas calculadas (opcional, para que el frontend no invente números)
    private List<StatDTO> stats;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatDTO {
        private String label;
        private String value;
    }
}