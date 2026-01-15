package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
    // Validar si un usuario pertenece a una conversación (Seguridad)
    boolean existsByConversation_IdConversationAndUser_IdUser(Long conversationId, Integer userId);
}