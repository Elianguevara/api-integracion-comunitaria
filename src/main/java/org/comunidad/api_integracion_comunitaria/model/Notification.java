package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "n_notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación: Usuario que recibe la notificación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    @ToString.Exclude
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "notification_type")
    private String notificationType;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // Relación opcional con Postulación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_postulation_id")
    @ToString.Exclude
    private Postulation relatedPostulation;

    // Relación opcional con Petición
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_petition_id")
    @ToString.Exclude
    private Petition relatedPetition;

    @Column(columnDefinition = "LONGTEXT")
    private String metadata;
}