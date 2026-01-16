package org.comunidad.api_integracion_comunitaria.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "n_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Integer idUser;

    private String name;

    private String lastname;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(unique = true)
    private String email;

    private String password;

    private Boolean enabled;

    @Column(name = "date_create")
    private LocalDateTime dateCreate;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_superuser")
    private Boolean isSuperuser;

    @Column(name = "date_joined")
    private LocalDateTime dateJoined;

    @Column(name = "is_staff")
    private Boolean isStaff;

    @Column(name = "is_active")
    private Boolean isActive;
}