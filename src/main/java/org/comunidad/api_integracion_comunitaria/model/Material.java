package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "n_material")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private Integer idMaterial;

    private String name;

    // Relación: Proveedor dueño del material
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_provider")
    @ToString.Exclude
    private Provider provider;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    private String unit;

    private String description;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}