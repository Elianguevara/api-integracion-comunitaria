package org.comunidad.api_integracion_comunitaria.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "n_postulation_state_history")
public class PostulationStateHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_history")
    private Integer idHistory;

    // Relación con la Postulación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_postulation")
    @ToString.Exclude
    private Postulation postulation;

    // Relación con el Estado asignado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_state")
    @ToString.Exclude
    private PostulationState state;

    // Relación con el Usuario que hizo el cambio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    @ToString.Exclude
    private User changedByUser;

    private String notes;

    @Column(name = "date_change")
    private LocalDateTime dateChange;
}