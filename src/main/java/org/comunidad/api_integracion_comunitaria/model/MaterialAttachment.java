package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "n_material_attachment")
public class MaterialAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material_attachment")
    private Integer idMaterialAttachment;

    // Relación: Material al que pertenece este adjunto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material")
    @ToString.Exclude
    private Material material;

    private String file;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    // Relación: Usuario que subió el archivo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_upload")
    @ToString.Exclude
    private User userUpload;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}