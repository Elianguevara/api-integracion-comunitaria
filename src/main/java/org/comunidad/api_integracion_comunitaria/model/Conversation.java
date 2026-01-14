package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "n_conversation")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversation")
    private Long idConversation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Relación Many-to-One: Una conversación pertenece a una petición
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "petition_id")
    @ToString.Exclude
    private Petition petition;
}