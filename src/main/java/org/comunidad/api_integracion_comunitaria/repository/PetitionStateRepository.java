package org.comunidad.api_integracion_comunitaria.repository;

import java.util.Optional;

import org.comunidad.api_integracion_comunitaria.model.PetitionState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetitionStateRepository extends JpaRepository<PetitionState, Integer> {
    Optional<PetitionState> findByName(String name);
}
