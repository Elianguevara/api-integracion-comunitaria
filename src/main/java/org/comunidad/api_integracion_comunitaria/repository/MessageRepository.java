package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Obtener historial de mensajes ordenados cronológicamente
    List<Message> findByConversation_IdConversationOrderByCreatedAtAsc(Long conversationId);
}