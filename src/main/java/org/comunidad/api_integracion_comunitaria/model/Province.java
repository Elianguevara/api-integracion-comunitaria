package org.comunidad.api_integracion_comunitaria.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "n_province")
public class Province {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_province")
    private Integer idProvince;

    private String name;

    // Relación con el País (asumiendo que existe la entidad Country)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_country")
    @ToString.Exclude
    private Country country;

    @Column(name = "date_create")
    private LocalDateTime dateCreate;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;
}