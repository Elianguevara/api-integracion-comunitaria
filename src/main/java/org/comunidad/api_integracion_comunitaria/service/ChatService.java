package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.response.ConversationResponse;
import org.comunidad.api_integracion_comunitaria.dto.response.MessageResponse;
import org.comunidad.api_integracion_comunitaria.model.*;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final PetitionRepository petitionRepository;
    private final UserRoleRepository userRoleRepository;

    private User getAuthenticatedUser() {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Transactional
    public ConversationResponse createOrGetConversation(Integer petitionId, Integer targetUserId) {
        User currentUser = getAuthenticatedUser();
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Usuario destino no encontrado"));
        Petition petition = petitionRepository.findById(petitionId)
                .orElseThrow(() -> new RuntimeException("Petición no encontrada"));

        Conversation conversation = conversationRepository.findExistingConversation(petitionId, currentUser.getIdUser(), targetUserId)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setPetition(petition);
                    newConv.setCreatedAt(LocalDateTime.now());
                    Conversation savedConv = conversationRepository.save(newConv);

                    ConversationParticipant p1 = new ConversationParticipant();
                    p1.setConversation(savedConv);
                    p1.setUser(currentUser);
                    participantRepository.save(p1);

                    ConversationParticipant p2 = new ConversationParticipant();
                    p2.setConversation(savedConv);
                    p2.setUser(targetUser);
                    participantRepository.save(p2);

                    return savedConv;
                });

        return mapToConversationResponse(conversation, currentUser.getIdUser());
    }

    @Transactional
    public MessageResponse sendMessage(Long conversationId, String content) {
        User currentUser = getAuthenticatedUser();

        if (!participantRepository.existsByConversation_IdConversationAndUser_IdUser(conversationId, currentUser.getIdUser())) {
            throw new RuntimeException("No tienes permiso para enviar mensajes en esta conversación.");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(currentUser);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        message.setIsRead(false);

        Message savedMsg = messageRepository.save(message);

        return mapToMessageResponse(savedMsg, currentUser.getIdUser());
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations() {
        User currentUser = getAuthenticatedUser();
        return conversationRepository.findByUserId(currentUser.getIdUser()).stream()
                .map(c -> mapToConversationResponse(c, currentUser.getIdUser()))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MessageResponse> getConversationMessages(Long conversationId) {
        User currentUser = getAuthenticatedUser();

        if (!participantRepository.existsByConversation_IdConversationAndUser_IdUser(conversationId, currentUser.getIdUser())) {
            throw new RuntimeException("Acceso denegado a esta conversación.");
        }

        return messageRepository.findByConversation_IdConversationOrderByCreatedAtAsc(conversationId).stream()
                .map(m -> mapToMessageResponse(m, currentUser.getIdUser()))
                .collect(Collectors.toList());
    }

    // --- Mappers ---
    private ConversationResponse mapToConversationResponse(Conversation c, Integer myUserId) {
        // 1. Buscar al otro participante
        User otherUser = participantRepository.findFirstByConversation_IdConversationAndUser_IdUserNot(c.getIdConversation(), myUserId)
                .map(ConversationParticipant::getUser)
                .orElse(null);

        // 2. Buscar el rol del otro participante usando el método exacto de tu repositorio
        String roleName = "";
        if (otherUser != null) {
            // AQUÍ ESTÁ EL CAMBIO: Usamos findByUser_IdUser pasándole el ID
            List<UserRole> roles = userRoleRepository.findByUser_IdUser(otherUser.getIdUser());
            if (roles != null && !roles.isEmpty()) {
                roleName = roles.get(0).getRole().getName();
            }
        }

        // 3. Obtener datos del último mensaje
        Optional<Message> lastMessageOpt = messageRepository.findTopByConversation_IdConversationOrderByCreatedAtDesc(c.getIdConversation());

        // 4. Contar no leídos
        Long unreadCount = messageRepository.countByConversation_IdConversationAndSender_IdUserNotAndIsReadFalse(c.getIdConversation(), myUserId);

        // 5. Calcular dinámicamente la última actividad
        LocalDateTime lastActivity = lastMessageOpt.map(Message::getCreatedAt).orElse(c.getCreatedAt());

        return ConversationResponse.builder()
                .idConversation(c.getIdConversation())
                .petitionId(c.getPetition().getIdPetition())
                .petitionTitle(c.getPetition().getDescription())
                .otherParticipantId(otherUser != null ? otherUser.getIdUser() : null)
                .otherParticipantName(otherUser != null ? otherUser.getName() : "Usuario Desconocido")
                .otherParticipantRole(roleName)
                .otherParticipantImage(otherUser != null ? otherUser.getProfileImage() : null)
                .lastMessage(lastMessageOpt.map(Message::getContent).orElse(""))
                .updatedAt(lastActivity)
                .unreadCount(unreadCount)
                .build();
    }

    private MessageResponse mapToMessageResponse(Message m, Integer myUserId) {
        return MessageResponse.builder()
                .idMessage(m.getIdMessage())
                .content(m.getContent())
                .sentAt(m.getCreatedAt())
                .senderId(m.getSender().getIdUser())
                .senderName(m.getSender().getName())
                .isMine(m.getSender().getIdUser().equals(myUserId))
                .build();
    }
}