package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "n_user_role")
@IdClass(UserRoleId.class) // Vincula con la clase de abajo
public class UserRole {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    @ToString.Exclude
    private User user; // Nombre del campo cambiado de 'idUser' a 'user' para reflejar el objeto

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_role")
    @ToString.Exclude
    private Role role; // Nombre del campo cambiado de 'idRole' a 'role'
}