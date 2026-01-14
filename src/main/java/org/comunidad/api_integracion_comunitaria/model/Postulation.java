package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "n_postulation")
public class Postulation extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_postulation")
    private Integer idPostulation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_petition")
    @ToString.Exclude
    private Petition petition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_provider")
    @ToString.Exclude
    private Provider provider;

    private Boolean winner;

    private String proposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_state")
    @ToString.Exclude
    private PostulationState state;

    private String current;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}