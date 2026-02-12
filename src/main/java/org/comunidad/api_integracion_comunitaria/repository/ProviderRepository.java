package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Provider;
import org.comunidad.api_integracion_comunitaria.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar la entidad Provider (Proveedores).
 * Incluye consultas optimizadas para el sistema de notificaciones y filtrado.
 */
@Repository
public interface ProviderRepository extends JpaRepository<Provider, Integer> {

    /**
     * Busca un proveedor asociado a un ID de usuario específico.
     * Útil para recuperar el perfil del proveedor desde el token JWT.
     */
    Optional<Provider> findByUser_IdUser(Integer idUser);

    /**
     * Busca un proveedor por la entidad User completa.
     */
    Optional<Provider> findByUser(User user);

    /**
     * 1. Buscar proveedores por Profesión.
     * Recupera todos los proveedores que ejercen una profesión específica.
     * * @param idProfession ID de la profesión (ej: 1 = Plomero).
     */
    List<Provider> findByProfession_IdProfession(Integer idProfession);

    /**
     * 2. Buscar proveedores por Ciudad.
     * Utiliza una Query personalizada para navegar la relación ManyToMany o OneToMany con ciudades.
     * * @param idCity ID de la ciudad.
     */
    @Query("SELECT p FROM Provider p JOIN p.providerCities pc WHERE pc.city.idCity = :idCity")
    List<Provider> findByCityId(@Param("idCity") Integer idCity);

    /**
     * 3. BÚSQUEDA COMBINADA (CRÍTICA PARA NOTIFICACIONES).
     * Encuentra proveedores que tengan la profesión X Y trabajen en la ciudad Y.
     * Se usa para notificar solo a los proveedores relevantes cuando se crea una solicitud.
     *
     * @param idProfession ID de la profesión requerida.
     * @param idCity ID de la ciudad donde es el trabajo.
     * @return Lista de proveedores que coinciden con ambos criterios.
     */
    @Query("SELECT p FROM Provider p " +
            "JOIN p.providerCities pc " +
            "WHERE p.profession.idProfession = :idProfession " +
            "AND pc.city.idCity = :idCity")
    List<Provider> findByProfessionAndCity(
            @Param("idProfession") Integer idProfession,
            @Param("idCity") Integer idCity
    );

    /**
     * 4. Buscar proveedores por Categoría (Opcional).
     * Si usas categorías (ej: Construcción, Tecnología) además de profesiones.
     */
    @Query("SELECT p FROM Provider p JOIN p.providerCategories pc WHERE pc.category.idCategory = :idCategory")
    List<Provider> findByCategoryId(@Param("idCategory") Integer idCategory);
}