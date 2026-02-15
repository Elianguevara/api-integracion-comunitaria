package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.GradeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeProviderRepository extends JpaRepository<GradeProvider, Integer> {

    // 1. Obtener calificaciones de un proveedor (PAGINADO)
    Page<GradeProvider> findByProvider_IdProvider(Integer idProvider, Pageable pageable);

    // 2. Calcular promedio de estrellas
    @Query("SELECT AVG(g.rating) FROM GradeProvider g WHERE g.provider.idProvider = :idProvider")
    Double getAverageRating(@Param("idProvider") Integer idProvider);

    // 3. Cuenta la cantidad total de reseñas del proveedor
    Integer countByProvider_IdProvider(Integer idProvider);

    // 4. Validación anti-spam: Verifica si el cliente ya lo calificó por ESTA petición específica
    boolean existsByCustomer_IdCustomerAndProvider_IdProviderAndPetition_IdPetition(
            Integer idCustomer, Integer idProvider, Integer idPetition);
}