package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "n_notification_settings")
public class NotificationSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación 1 a 1: Configuración pertenece a un usuario
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    @ToString.Exclude
    private User user;

    @Column(name = "postulation_created")
    private Boolean postulationCreated;

    @Column(name = "postulation_state_changed")
    private Boolean postulationStateChanged;

    @Column(name = "postulation_accepted")
    private Boolean postulationAccepted;

    @Column(name = "postulation_rejected")
    private Boolean postulationRejected;

    @Column(name = "petition_created")
    private Boolean petitionCreated;

    @Column(name = "petition_closed")
    private Boolean petitionClosed;

    @Column(name = "email_notifications")
    private Boolean emailNotifications;

    @Column(name = "push_notifications")
    private Boolean pushNotifications;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}