package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.GradeCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeCustomerRepository extends JpaRepository<GradeCustomer, Integer> {
    List<GradeCustomer> findByCustomer_IdCustomer(Integer idCustomer);
}