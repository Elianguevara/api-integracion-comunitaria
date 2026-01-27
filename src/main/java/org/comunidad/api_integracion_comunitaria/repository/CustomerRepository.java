package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Customer;
import org.comunidad.api_integracion_comunitaria.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    // Buscar cliente por el ID de su usuario (útil para sacar el perfil desde el
    // token)
    Optional<Customer> findByUser_IdUser(Integer idUser);

    // Buscar por DNI (útil para validaciones)
    Optional<Customer> findByDni(String dni);
    Optional<Customer> findByUser(User user);
}