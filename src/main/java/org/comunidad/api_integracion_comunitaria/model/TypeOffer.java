package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "n_type_offer")
public class TypeOffer extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_offer")
    private Integer idTypeOffer;

    private String name;
}