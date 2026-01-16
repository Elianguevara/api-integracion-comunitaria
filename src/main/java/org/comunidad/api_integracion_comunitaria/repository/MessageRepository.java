package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Obtener historial de mensajes paginado.
    // IMPORTANTE: El ordenamiento (DESC o ASC) vendrá definido en el Pageable desde
    // el Controller.
    Page<Message> findByConversation_IdConversation(Long conversationId, Pageable pageable);
}