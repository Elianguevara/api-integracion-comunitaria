package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Buscar conversaciones de un usuario específico
    @Query("SELECT c FROM Conversation c JOIN ConversationParticipant cp ON c.idConversation = cp.conversation.idConversation WHERE cp.user.idUser = :userId ORDER BY c.createdAt DESC")
    List<Conversation> findByUserId(@Param("userId") Integer userId);

    // Verificar si ya existe una conversación para una Petición entre dos usuarios
    // (para no duplicar)
    // Esta query busca una conversación que tenga la petición X y donde existan
    // participantes A y B
    @Query("SELECT c FROM Conversation c " +
            "JOIN ConversationParticipant cp1 ON c.idConversation = cp1.conversation.idConversation " +
            "JOIN ConversationParticipant cp2 ON c.idConversation = cp2.conversation.idConversation " +
            "WHERE c.petition.idPetition = :petitionId " +
            "AND cp1.user.idUser = :user1Id " +
            "AND cp2.user.idUser = :user2Id")
    Optional<Conversation> findExistingConversation(@Param("petitionId") Integer petitionId,
            @Param("user1Id") Integer user1Id,
            @Param("user2Id") Integer user2Id);
}