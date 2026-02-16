package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c JOIN ConversationParticipant cp ON c.idConversation = cp.conversation.idConversation WHERE cp.user.idUser = :userId")
    List<Conversation> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT c FROM Conversation c " +
            "JOIN ConversationParticipant cp1 ON c.idConversation = cp1.conversation.idConversation " +
            "JOIN ConversationParticipant cp2 ON c.idConversation = cp2.conversation.idConversation " +
            "WHERE c.petition.idPetition = :petitionId " +
            "AND cp1.user.idUser = :userId1 AND cp2.user.idUser = :userId2")
    List<Conversation> findExistingConversation(@Param("petitionId") Integer petitionId,
                                                @Param("userId1") Integer userId1,
                                                @Param("userId2") Integer userId2);
}