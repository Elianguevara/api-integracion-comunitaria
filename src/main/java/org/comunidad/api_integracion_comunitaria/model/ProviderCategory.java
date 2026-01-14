package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "n_provider_category") // Corregí el typo "cateogory" en el nombre de tabla si estaba mal
public class ProviderCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación con el Proveedor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    @ToString.Exclude
    private Provider provider;

    // Relación con la Categoría
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;
}