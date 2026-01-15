package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.GradeProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeProviderRepository extends JpaRepository<GradeProvider, Integer> {

    // Obtener todas las calificaciones de un proveedor
    List<GradeProvider> findByProvider_IdProvider(Integer idProvider);

    // Calcular el promedio de estrellas (Útil para mostrar "4.5 estrellas" en el
    // perfil)
    @Query("SELECT AVG(g.rating) FROM GradeProvider g WHERE g.provider.idProvider = :idProvider")
    Double getAverageRating(@Param("idProvider") Integer idProvider);

    // Verificar si el cliente ya calificó a este proveedor (para evitar spam)
    // Nota: Idealmente deberíamos ligar esto a una Petición específica, pero por
    // ahora limitamos a 1 por pareja
    boolean existsByCustomer_IdCustomerAndProvider_IdProvider(Integer idCustomer, Integer idProvider);
}