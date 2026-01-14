package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "n_profession")
public class Profession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profession")
    private Integer idProfession;

    private String name;
}