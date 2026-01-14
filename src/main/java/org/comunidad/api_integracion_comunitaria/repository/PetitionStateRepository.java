package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.PetitionState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PetitionStateRepository extends JpaRepository<PetitionState, Integer> {

    // Método necesario para buscar estados por su nombre (ej: "PUBLICADA",
    // "PENDIENTE")
    Optional<PetitionState> findByName(String name);
}