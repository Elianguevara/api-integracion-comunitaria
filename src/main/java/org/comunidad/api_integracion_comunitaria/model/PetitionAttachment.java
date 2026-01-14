package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "n_petition_attachment")
public class PetitionAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_petition_attachment")
    private Integer idPetitionAttachment;

    // Vincula el adjunto a la petición padre
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_petition")
    @ToString.Exclude
    private Petition petition;

    private String url;

    // --- Auditoría ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_create")
    @ToString.Exclude
    private User userCreate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_update")
    @ToString.Exclude
    private User userUpdate;

    @Column(name = "date_create")
    private LocalDateTime dateCreate;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;
}