package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "n_petition_state")
public class PetitionState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_state")
    private Integer idState;

    private String name;

    private String description;
}