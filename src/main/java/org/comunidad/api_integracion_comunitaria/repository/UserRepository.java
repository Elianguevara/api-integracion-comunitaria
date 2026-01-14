package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // Método mágico de JPA para buscar por email
    Optional<User> findByEmail(String email);
}