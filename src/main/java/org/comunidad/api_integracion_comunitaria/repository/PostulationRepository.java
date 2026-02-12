package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Postulation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostulationRepository extends JpaRepository<Postulation, Integer> {

    boolean existsByPetition_IdPetitionAndProvider_IdProvider(Integer idPetition, Integer idProvider);

    Page<Postulation> findByPetition_IdPetition(Integer idPetition, Pageable pageable);

    List<Postulation> findByPetition_IdPetition(Integer idPetition);

    List<Postulation> findByPetition_IdPetitionAndIdPostulationNot(Integer idPetition, Integer idPostulationWinner);

    Page<Postulation> findByProvider_IdProvider(Integer idProvider, Pageable pageable);

    // --- EL MÉTODO QUE FALTABA ---
    // Verifica si un proveedor ya ganó un trabajo con este cliente
    boolean existsByPetition_Customer_IdCustomerAndProvider_IdProviderAndWinnerTrue(Integer idCustomer, Integer idProvider);
}