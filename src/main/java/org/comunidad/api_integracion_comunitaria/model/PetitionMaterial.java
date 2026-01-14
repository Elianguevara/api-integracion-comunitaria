package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "n_petition_material")
public class PetitionMaterial extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_petition_material")
    private Integer idPetitionMaterial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_petition")
    @ToString.Exclude
    private Petition petition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_article")
    @ToString.Exclude
    private Material material;

    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;
}