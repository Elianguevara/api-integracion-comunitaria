package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.PetitionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar los Estados de las Peticiones (Tabla n_petition_state).
 * Se utiliza para validar transiciones de estado (ej: de Creada a Publicada, Cancelada, Finalizada).
 */
@Repository
public interface PetitionStateRepository extends JpaRepository<PetitionState, Integer> {

    /**
     * Busca un estado por su nombre exacto (ej: "PUBLICADA", "CANCELADA").
     * Es vital para la lógica de negocio en PetitionService.
     *
     * @param name Nombre del estado.
     * @return Optional con el estado si existe.
     */
    Optional<PetitionState> findByName(String name);
}