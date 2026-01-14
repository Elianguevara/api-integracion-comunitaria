package org.comunidad.api_integracion_comunitaria.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "n_postulation_material")
public class PostulationMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_postulation_material")
    private Integer idPostulationMaterial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulation")
    @ToString.Exclude
    private Postulation postulation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material")
    @ToString.Exclude
    private Material material;

    private BigDecimal quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    private BigDecimal total;

    private String notes;
}