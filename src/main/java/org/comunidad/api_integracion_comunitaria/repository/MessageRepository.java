package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Para cargar la sala de chat
    List<Message> findByConversation_IdConversationOrderByCreatedAtAsc(Long conversationId);

    // Para la bandeja de entrada: obtener el último mensaje (el "preview")
    Optional<Message> findTopByConversation_IdConversationOrderByCreatedAtDesc(Long conversationId);

    // Para la bandeja de entrada: contar mensajes no leídos
    Long countByConversation_IdConversationAndSender_IdUserNotAndIsReadFalse(Long conversationId, Integer myUserId);
}