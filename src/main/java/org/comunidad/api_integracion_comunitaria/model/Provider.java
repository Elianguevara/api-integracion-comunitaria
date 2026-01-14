package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "n_provider")
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_provider")
    private Integer idProvider;

    // Relación 1 a 1 con el Usuario
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id_user")
    @ToString.Exclude
    private User user;

    // Relación con el Tipo de Proveedor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_provider")
    @ToString.Exclude
    private TypeProvider typeProvider;

    // Relación con la Profesión principal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profession")
    @ToString.Exclude
    private Profession profession;

    // Relación con la Dirección (Si se borra el proveedor, se borra su dirección)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    @ToString.Exclude
    private Address address;

    @Column(columnDefinition = "TEXT")
    private String description;
}