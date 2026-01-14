package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "n_offers")
public class Offer extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Integer offerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_offer")
    @ToString.Exclude
    private TypeOffer typeOffer;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_open")
    private LocalDateTime dateOpen;

    @Column(name = "date_close")
    private LocalDateTime dateClose;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_provider")
    @ToString.Exclude
    private Provider provider;

    // Los campos de auditoría (userCreate, userUpdate, dates) se heredan.
    // NOTA: La clase padre espera las columnas 'id_user_create' e 'id_user_update'.

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}