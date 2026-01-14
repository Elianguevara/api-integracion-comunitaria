package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "n_address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_address")
    private Integer idAddress;

    private String street;
    private String number;
    private String floor;
    private String apartment;

    @Column(name = "postal_code")
    private String postalCode;

    // CORRECCIÓN PRINCIPAL: Relación con City
    @ManyToOne(fetch = FetchType.LAZY) // Lazy carga la ciudad solo si la pides (rendimiento)
    @JoinColumn(name = "id_city")
    @ToString.Exclude
    private City city;

    @Column(name = "date_create")
    private LocalDateTime dateCreate;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;
}