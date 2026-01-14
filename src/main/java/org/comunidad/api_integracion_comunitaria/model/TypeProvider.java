package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "n_type_provider")
public class TypeProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_provider")
    private Integer idTypeProvider;

    private String name;
}