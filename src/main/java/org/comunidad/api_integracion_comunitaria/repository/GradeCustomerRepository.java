package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.GradeCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeCustomerRepository extends JpaRepository<GradeCustomer, Integer> {

    // 1. Obtiene las calificaciones que ha recibido un cliente
    List<GradeCustomer> findByCustomer_IdCustomer(Integer idCustomer);

    // 2. Validación anti-spam: Verifica si el proveedor ya calificó a este cliente por ESTA petición
    boolean existsByProvider_IdProviderAndCustomer_IdCustomerAndPetition_IdPetition(
            Integer idProvider, Integer idCustomer, Integer idPetition);
}