package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Integer> {
    // Buscar proveedor por ID de usuario
    Optional<Provider> findByUser_IdUser(Integer idUser);
}