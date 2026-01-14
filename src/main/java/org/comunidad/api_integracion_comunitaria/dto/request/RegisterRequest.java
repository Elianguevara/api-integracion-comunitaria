package org.comunidad.api_integracion_comunitaria.dto.request; // Asegúrate de que el paquete sea correcto

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String name;
    private String lastname;
    private String email;
    private String password;

    // Valores esperados: "PROVIDER" o "CUSTOMER"
    private String role;
}