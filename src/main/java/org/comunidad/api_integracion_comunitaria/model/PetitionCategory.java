package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "n_petition_category")
public class PetitionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_petition_category")
    private Integer idPetitionCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_petition")
    @ToString.Exclude
    private Petition petition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category")
    @ToString.Exclude
    private Category category;
}