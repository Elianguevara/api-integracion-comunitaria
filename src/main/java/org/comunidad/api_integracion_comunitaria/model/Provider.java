package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.util.List; // Importante para las listas

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

    // Relación con la Dirección
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    @ToString.Exclude
    private Address address;

    @Column(columnDefinition = "TEXT")
    private String description;

    // --- AGREGADO: Relaciones Inversas para las Queries del Repository ---

    // 1. Relación con Categorías (para 'p.providerCategories')
    @OneToMany(mappedBy = "provider", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ProviderCategory> providerCategories;

    // 2. Relación con Ciudades (para 'p.providerCities')
    @OneToMany(mappedBy = "provider", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ProviderCity> providerCities;
}