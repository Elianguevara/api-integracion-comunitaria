package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "n_message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_message")
    private Long idMessage;

    // Relación: Conversación a la que pertenece
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    @ToString.Exclude
    private Conversation conversation;

    // Relación: Usuario que envía el mensaje
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @ToString.Exclude
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // CORRECCIÓN: Evitar nombre de columna "read" (reservado SQL)
    @Column(name = "is_read")
    private Boolean isRead;
}