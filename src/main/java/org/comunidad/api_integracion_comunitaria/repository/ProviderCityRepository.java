package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.ProviderCity;
import org.comunidad.api_integracion_comunitaria.model.ProviderCityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderCityRepository extends JpaRepository<ProviderCity, ProviderCityId> {
    // Para borrar las ciudades anteriores si el proveedor actualiza su perfil
    void deleteByProvider_IdProvider(Integer idProvider);

    // Para recuperar las ciudades de un proveedor
    List<ProviderCity> findByProvider_IdProvider(Integer idProvider);
}