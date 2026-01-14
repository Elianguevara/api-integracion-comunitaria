package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "n_city")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_city")
    private Integer idCity;

    private String name;

    @Column(name = "postal_code")
    private String postalCode;

    // Relación Many-to-One: Muchas ciudades pertenecen a un departamento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_department")
    @ToString.Exclude
    private Department department;

    @Column(name = "date_create")
    private LocalDateTime dateCreate;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;
}