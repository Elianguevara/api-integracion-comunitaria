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

    // --- NUEVOS CAMPOS ---
    private Integer providerId;
    private Integer customerId;
    // ---------------------

    private String name;
    private String lastname;
    private String email;
    private String role;
    private String profileImage;

    // Campos específicos
    private String phone;
    private String address;
    private String description;
    private String profession;

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