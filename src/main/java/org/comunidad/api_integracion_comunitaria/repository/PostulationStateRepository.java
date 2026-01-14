package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.PostulationState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PostulationStateRepository extends JpaRepository<PostulationState, Integer> {
    Optional<PostulationState> findByName(String name);
}