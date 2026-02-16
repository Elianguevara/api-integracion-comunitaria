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
    private final ProviderRepository providerRepository;
    private final PostulationRepository postulationRepository; // <-- NUEVO: Inyectamos postulaciones

    private User getAuthenticatedUser() {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // --- NUEVO: MÉTODO QUE VALIDA SI EL CHAT DEBE ESTAR ABIERTO ---
    private boolean isConversationActive(Conversation c) {
        Petition petition = c.getPetition();
        String state = petition.getState().getName().toUpperCase();

        // Si finalizó o se canceló, nadie puede hablar
        if (state.equals("FINALIZADA") || state.equals("CANCELADA")) {
            return false;
        }

        // Si está adjudicada, solo pueden hablar si el proveedor ganador está en este chat
        if (state.equals("ADJUDICADA")) {
            List<Postulation> postulations = postulationRepository.findByPetition_IdPetition(petition.getIdPetition());
            Integer winnerUserId = null;

            for (Postulation p : postulations) {
                if (p.getWinner() != null && p.getWinner()) {
                    winnerUserId = p.getProvider().getUser().getIdUser();
                    break;
                }
            }

            if (winnerUserId != null) {
                // Si el usuario ganador pertenece a ESTA conversación, sigue activa
                return participantRepository.existsByConversation_IdConversationAndUser_IdUser(c.getIdConversation(), winnerUserId);
            }
            return false;
        }

        // Si está PUBLICADA o EN_POSTULACIONES, todos pueden hablar
        return true;
    }

    @Transactional
    public ConversationResponse createOrGetConversation(Integer petitionId, Integer providerId) {
        User currentUser = getAuthenticatedUser();

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        User targetUser = provider.getUser();

        Petition petition = petitionRepository.findById(petitionId)
                .orElseThrow(() -> new RuntimeException("Petición no encontrada"));

        Conversation conversation = conversationRepository.findExistingConversation(petitionId, currentUser.getIdUser(), targetUser.getIdUser())
                .stream()
                .findFirst()
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

        // --- NUEVO: BLOQUEAMOS EL ENVÍO DESDE EL BACKEND SI ESTÁ CERRADO ---
        if (!isConversationActive(conversation)) {
            throw new RuntimeException("Esta conversación ha sido cerrada. La solicitud finalizó o fue adjudicada a otro proveedor.");
        }

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

    @Transactional
    public void markMessagesAsRead(Long conversationId) {
        User currentUser = getAuthenticatedUser();
        if (!participantRepository.existsByConversation_IdConversationAndUser_IdUser(conversationId, currentUser.getIdUser())) {
            throw new RuntimeException("Acceso denegado a esta conversación.");
        }
        List<Message> unreadMessages = messageRepository.findByConversation_IdConversationAndSender_IdUserNotAndIsReadFalse(
                conversationId,
                currentUser.getIdUser()
        );
        if (!unreadMessages.isEmpty()) {
            unreadMessages.forEach(m -> m.setIsRead(true));
            messageRepository.saveAll(unreadMessages);
        }
    }

    private ConversationResponse mapToConversationResponse(Conversation c, Integer myUserId) {
        User otherUser = participantRepository.findFirstByConversation_IdConversationAndUser_IdUserNot(c.getIdConversation(), myUserId)
                .map(ConversationParticipant::getUser)
                .orElse(null);

        String roleName = "";
        if (otherUser != null) {
            List<UserRole> roles = userRoleRepository.findByUser_IdUser(otherUser.getIdUser());
            if (roles != null && !roles.isEmpty()) {
                roleName = roles.get(0).getRole().getName();
            }
        }

        Optional<Message> lastMessageOpt = messageRepository.findTopByConversation_IdConversationOrderByCreatedAtDesc(c.getIdConversation());
        Long unreadCount = messageRepository.countByConversation_IdConversationAndSender_IdUserNotAndIsReadFalse(c.getIdConversation(), myUserId);
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
                .isReadOnly(!isConversationActive(c)) // <-- NUEVO: ENVIAMOS EL ESTADO AL FRONT
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