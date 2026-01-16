package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Petition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Petition}.
 * <p>
 * Extiende de JpaRepository para operaciones CRUD básicas e implementa
 * métodos de búsqueda paginados para optimizar el rendimiento en el frontend.
 * </p>
 */
@Repository
public interface PetitionRepository extends JpaRepository<Petition, Integer> {

    /**
     * Busca todas las peticiones creadas por un cliente específico de forma
     * paginada.
     * Útil para la vista "Mis Peticiones" en el panel del cliente.
     *
     * @param idCustomer ID del cliente propietario de las peticiones.
     * @param pageable   Objeto de configuración de paginación (página, tamaño,
     *                   orden).
     * @return Una página (Page) de peticiones.
     */
    Page<Petition> findByCustomer_IdCustomer(Integer idCustomer, Pageable pageable);

    /**
     * Busca peticiones por el nombre de su estado.
     * Es el método principal para el "Feed de trabajos" (ej: buscar solo
     * "PUBLICADA").
     *
     * @param stateName Nombre del estado (ej: "PUBLICADA", "ADJUDICADA").
     * @param pageable  Objeto de configuración de paginación.
     * @return Una página de peticiones filtradas por estado.
     */
    Page<Petition> findByState_Name(String stateName, Pageable pageable);

    /**
     * Filtro avanzado: Busca peticiones por Categoría (Tipo) y Estado.
     * Permite al proveedor filtrar trabajos, ej: "Ver solo Plomería que estén
     * Publicadas".
     *
     * @param idType    ID del tipo de petición (Categoría).
     * @param stateName Nombre del estado.
     * @param pageable  Objeto de configuración de paginación.
     * @return Una página de peticiones que cumplen ambos criterios.
     */
    Page<Petition> findByTypePetition_IdTypePetitionAndState_Name(Integer idType, String stateName, Pageable pageable);

    /**
     * Busca todas las peticiones que no han sido marcadas como eliminadas (Soft
     * Delete).
     *
     * @param pageable Objeto de configuración de paginación.
     * @return Página de peticiones activas.
     */
    Page<Petition> findByIsDeletedFalse(Pageable pageable);
}