package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Petition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PetitionRepository extends JpaRepository<Petition, Integer> {

    // 1. Para que el Cliente vea SUS peticiones
    List<Petition> findByCustomer_IdCustomer(Integer idCustomer);

    // 2. Para el "Feed" público: Peticiones activas (filtra por nombre de estado)
    // Asumiendo que el estado publicado se llama "PUBLICADA"
    List<Petition> findByState_Name(String stateName);

    // 3. Filtros avanzados (ejemplo: buscar por categoría/tipo de petición y
    // estado)
    List<Petition> findByTypePetition_IdTypePetitionAndState_Name(Integer idType, String stateName);

    // Opcional: Una query nativa o JPQL si necesitas filtrar las que NO están
    // borradas
    // Aunque tu entidad ya tiene 'isDeleted', JPA lo maneja si lo pides:
    List<Petition> findByIsDeletedFalse();
}