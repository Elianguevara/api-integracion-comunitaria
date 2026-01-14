package org.comunidad.api_integracion_comunitaria.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "n_petition_state_history")
public class PetitionStateHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_petition_state_history")
    private Integer idPetitionStateHistory;

    // Relación con la petición
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_petition")
    @ToString.Exclude
    private Petition petition;

    // Relación con el nuevo estado asignado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_state")
    @ToString.Exclude
    private PetitionState state;

    // Relación con el usuario que hizo el cambio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    @ToString.Exclude
    private User changedByUser;

    private String note;

    @Column(name = "change_date")
    private LocalDateTime changeDate;
}