package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "n_petition")
public class Petition extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_petition")
    private Integer idPetition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_petition")
    @ToString.Exclude
    private TypePetition typePetition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_customer")
    @ToString.Exclude
    private Customer customer;

    // --- NUEVO CAMPO: CIUDAD ---
    // Fundamental para el filtro de notificaciones por zona
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_city")
    @ToString.Exclude
    private City city;
    // ---------------------------

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profession")
    @ToString.Exclude
    private Profession profession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_provider")
    @ToString.Exclude
    private TypeProvider typeProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_state")
    @ToString.Exclude
    private PetitionState state;

    @Column(name = "date_since")
    private LocalDate dateSince;

    @Column(name = "date_until")
    private LocalDate dateUntil;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}