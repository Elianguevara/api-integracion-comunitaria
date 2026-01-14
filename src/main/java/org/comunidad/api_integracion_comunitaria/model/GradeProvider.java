package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "n_grade_provider")
public class GradeProvider extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grade_provider")
    private Integer idGradeProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_provider")
    @ToString.Exclude
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_customer")
    @ToString.Exclude
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grade")
    @ToString.Exclude
    private Grade grade;

    private Integer rating;

    @Column(name = "coment")
    private String comment;

    private String response;

    @Column(name = "is_visible")
    private Boolean isVisible;
}