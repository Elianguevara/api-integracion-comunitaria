package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "n_availabilities")
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_availability")
    private Integer idAvailability;

    // CORRECCIÓN PRINCIPAL: Relación con Provider
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_provider", nullable = false)
    @ToString.Exclude
    private Provider provider;

    @Column(name = "day_of_week")
    private Integer dayOfWeek; // 1=Lunes, 7=Domingo

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    // AUDITORÍA COMO RELACIONES (Mucho más potente)
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