package org.comunidad.api_integracion_comunitaria.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de Transferencia de Datos (DTO) para la respuesta de autenticación.
 * <p>
 * Se utiliza tanto para el login como para el registro.
 * Devuelve el token JWT necesario para las peticiones seguras y el rol
 * del usuario para que el frontend pueda dirigirlo a la pantalla correcta.
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    /**
     * Token JWT (Json Web Token) formato Bearer.
     * Debe enviarse en el header Authorization de las peticiones subsiguientes.
     */
    private String token;

    /**
     * Rol principal del usuario en el sistema.
     * <p>
     * Valores posibles esperados por el frontend:
     * <ul>
     * <li>"CUSTOMER" - Para usuarios finales (Clientes).</li>
     * <li>"PROVIDER" - Para profesionales (Proveedores).</li>
     * </ul>
     * </p>
     */
    private String role;
    // Agregamos datos del usuario
    private String name;
    private String email;
}