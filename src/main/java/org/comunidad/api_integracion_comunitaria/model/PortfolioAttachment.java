package org.comunidad.api_integracion_comunitaria.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "n_portfolio_attachment")
public class PortfolioAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_attachment")
    private Integer idAttachment;

    // Relación con el Portafolio padre
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_portfolio")
    @ToString.Exclude
    private Portfolio portfolio;

    private String file;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    // Relación con el usuario que subió el archivo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_upload")
    @ToString.Exclude
    private User userUpload;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}