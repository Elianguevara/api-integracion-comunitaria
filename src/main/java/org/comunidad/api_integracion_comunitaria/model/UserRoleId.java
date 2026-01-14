package org.comunidad.api_integracion_comunitaria.model;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleId implements Serializable {
    private Integer user; // Debe coincidir con el nombre de la variable en UserRole (user)
    private Integer role; // Debe coincidir con el nombre de la variable en UserRole (role)
}