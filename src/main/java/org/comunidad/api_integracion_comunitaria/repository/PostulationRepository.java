package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Postulation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostulationRepository extends JpaRepository<Postulation, Integer> {

    // Ver si este proveedor ya se postuló a esta petición (Validación única)
    Optional<Postulation> findByPetition_IdPetitionAndProvider_IdProvider(Integer idPetition, Integer idProvider);

    // 1. Para el Cliente: Ver quién se postuló a su petición (Paginado)
    Page<Postulation> findByPetition_IdPetition(Integer idPetition, Pageable pageable);

    // 2. Para el Proveedor: Ver su historial de postulaciones (Paginado)
    // Esto faltaba en tu Service, es muy útil para el perfil del proveedor.
    Page<Postulation> findByProvider_IdProvider(Integer idProvider, Pageable pageable);

    // Validación extra
    boolean existsByPetition_Customer_IdCustomerAndProvider_IdProviderAndWinnerTrue(Integer idCustomer,
            Integer idProvider);
}