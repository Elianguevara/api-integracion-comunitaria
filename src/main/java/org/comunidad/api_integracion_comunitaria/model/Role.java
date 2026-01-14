package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "n_role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Integer idRole;

    private String name;
}