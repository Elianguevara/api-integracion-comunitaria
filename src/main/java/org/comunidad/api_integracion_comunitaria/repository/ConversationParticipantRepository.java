package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    boolean existsByConversation_IdConversationAndUser_IdUser(Long conversationId, Integer userId);

    // Para saber con quién estoy hablando
    Optional<ConversationParticipant> findFirstByConversation_IdConversationAndUser_IdUserNot(Long conversationId, Integer myUserId);
}