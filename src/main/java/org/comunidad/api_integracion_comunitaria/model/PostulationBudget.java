package org.comunidad.api_integracion_comunitaria.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "n_postulation_budget")
public class PostulationBudget extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_budget")
    private Integer idBudget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulation")
    @ToString.Exclude
    private Postulation postulation;

    @Column(name = "cost_type")
    private String costType;

    private BigDecimal amount;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal hours;

    @Column(name = "item_description")
    private String itemDescription;

    private String notes;
}