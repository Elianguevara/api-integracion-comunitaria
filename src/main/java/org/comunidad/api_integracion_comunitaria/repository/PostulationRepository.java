package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Postulation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PostulationRepository extends JpaRepository<Postulation, Integer> {
    // Ver si este proveedor ya se postuló a esta petición (para evitar duplicados)
    Optional<Postulation> findByPetition_IdPetitionAndProvider_IdProvider(Integer idPetition, Integer idProvider);

    // Listar todas las postulaciones de una petición (Para que el cliente elija)
    List<Postulation> findByPetition_IdPetition(Integer idPetition);

    // Ver historial de postulaciones de un proveedor
    List<Postulation> findByProvider_IdProvider(Integer idProvider);

    // En PostulationRepository.java
    boolean existsByPetition_Customer_IdCustomerAndProvider_IdProviderAndWinnerTrue(Integer idCustomer,
            Integer idProvider);
}