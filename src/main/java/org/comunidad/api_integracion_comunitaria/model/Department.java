package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "n_department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_department")
    private Integer idDepartment;

    private String name;

    // Relación con Provincia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_province")
    @ToString.Exclude
    private Province province;

    // Relación con País (Asumiendo que existe la clase Country)
    // Nota: A veces es redundante si Provincia ya tiene Country, pero si la DB lo
    // tiene, mapealo así:
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_country")
    @ToString.Exclude
    private Country country;

    @Column(name = "date_create")
    private LocalDateTime dateCreate;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;
}