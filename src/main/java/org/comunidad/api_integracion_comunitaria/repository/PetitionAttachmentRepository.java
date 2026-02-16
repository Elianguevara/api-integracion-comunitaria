package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.PetitionAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetitionAttachmentRepository extends JpaRepository<PetitionAttachment, Integer> {
    // Te servirá para buscar las fotos de una petición cuando devuelvas la respuesta
    List<PetitionAttachment> findByPetition_IdPetition(Integer idPetition);
}