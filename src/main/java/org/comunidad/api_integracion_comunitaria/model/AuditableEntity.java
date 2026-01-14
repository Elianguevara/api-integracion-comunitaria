package org.comunidad.api_integracion_comunitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@MappedSuperclass // ¡Crucial! Indica que esta clase no es una tabla, sino una plantilla.
public abstract class AuditableEntity {

    @Column(name = "date_create", updatable = false)
    private LocalDateTime dateCreate;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;

    // Relación con el usuario que creó el registro
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_create", updatable = false)
    @ToString.Exclude
    private User userCreate;

    // Relación con el usuario que actualizó el registro
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_update")
    @ToString.Exclude
    private User userUpdate;

    // Opcional: Método para asignar fechas automáticamente antes de guardar
    @PrePersist
    protected void onCreate() {
        this.dateCreate = LocalDateTime.now();
        this.dateUpdate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateUpdate = LocalDateTime.now();
    }
}