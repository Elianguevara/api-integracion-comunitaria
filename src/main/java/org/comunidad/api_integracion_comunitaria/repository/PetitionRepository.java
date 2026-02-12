package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Petition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetitionRepository extends JpaRepository<Petition, Integer> {

    /**
     * Recupera las peticiones de un cliente específico.
     * Se usa en el Dashboard del Cliente ("Mis Peticiones").
     */
    Page<Petition> findByCustomer_IdCustomer(Integer idCustomer, Pageable pageable);

    /**
     * Recupera peticiones por el nombre del estado (ej: "PUBLICADA").
     */
    Page<Petition> findByState_Name(String stateName, Pageable pageable);

    /**
     * FEED INTELIGENTE (Crucial para Proveedores):
     * Busca peticiones activas ("PUBLICADA"), pero EXCLUYE las que creó el propio usuario
     * que está consultando (para que un electricista no vea su propia solicitud de arreglo de luz).
     *
     * @param stateName Nombre del estado (ej: "PUBLICADA")
     * @param userId ID del usuario a excluir (el que está logueado)
     */
    Page<Petition> findByState_NameAndCustomer_User_IdUserNot(String stateName, Integer userId, Pageable pageable);

    /**
     * Recupera todas las peticiones que no han sido borradas lógicamente.
     * Útil para administración o auditoría.
     */
    Page<Petition> findByIsDeletedFalse(Pageable pageable);
}