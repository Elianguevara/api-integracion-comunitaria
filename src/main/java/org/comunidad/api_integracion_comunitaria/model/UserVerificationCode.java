package org.comunidad.api_integracion_comunitaria.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "n_user_verification_code")
public class UserVerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    private String code;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_used")
    private Boolean isUsed;
}