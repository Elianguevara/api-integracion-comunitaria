package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Petition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetitionRepository extends JpaRepository<Petition, Integer> {

    // ESTO YA LO TIENES (Lo usaremos para "Mis Peticiones")
    Page<Petition> findByCustomer_IdCustomer(Integer idCustomer, Pageable pageable);

    // ESTO YA LO TIENES
    Page<Petition> findByState_Name(String stateName, Pageable pageable);

    // ESTO YA LO TIENES
    Page<Petition> findByTypePetition_IdTypePetitionAndState_Name(Integer idType, String stateName, Pageable pageable);

    // --- AGREGAR ESTE NUEVO (Para el Feed Inteligente) ---
    /**
     * Busca peticiones por estado, excluyendo las del usuario actual.
     */
    Page<Petition> findByState_NameAndCustomer_User_IdUserNot(String stateName, Integer userId, Pageable pageable);

    Page<Petition> findByIsDeletedFalse(Pageable pageable);
}