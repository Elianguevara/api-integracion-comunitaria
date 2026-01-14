package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Integer> {
    // Buscar proveedor por ID de usuario
    Optional<Provider> findByUser_IdUser(Integer idUser);

    // 1. Buscar proveedores por Profesión (Ej: Todos los Plomeros)
    // Asumimos que tu entidad Provider tiene un campo 'profession'
    List<Provider> findByProfession_IdProfession(Integer idProfession);

    // 2. Buscar proveedores por Categoría
    // NOTA: Esto depende de cómo tengas mapeada la relación en Provider.
    // Si es ManyToMany directa:
    // List<Provider> findByCategories_IdCategory(Integer idCategory);

    // Si usas una tabla intermedia manual (ProviderCategory), usamos una Query
    // personalizada:
    @Query("SELECT p FROM Provider p JOIN p.providerCategories pc WHERE pc.category.idCategory = :idCategory")
    List<Provider> findByCategoryId(@Param("idCategory") Integer idCategory);

    // 3. Buscar proveedores por Ciudad (Para no notificar a un plomero de otra
    // provincia)
    @Query("SELECT p FROM Provider p JOIN p.providerCities pc WHERE pc.city.idCity = :idCity")
    List<Provider> findByCityId(@Param("idCity") Integer idCity);
}