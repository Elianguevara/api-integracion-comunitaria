package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Postulation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PostulationRepository extends JpaRepository<Postulation, Integer> {

    // 1. Ver todas las postulaciones para una Petición específica (Para que el
    // cliente elija)
    List<Postulation> findByPetition_IdPetition(Integer idPetition);

    // 2. Ver las postulaciones que ha hecho un Proveedor (Historial del proveedor)
    List<Postulation> findByProvider_IdProvider(Integer idProvider);

    // 3. Verificar si un proveedor YA se postuló a una petición (Evitar duplicados)
    Optional<Postulation> findByPetition_IdPetitionAndProvider_IdProvider(Integer idPetition, Integer idProvider);
}