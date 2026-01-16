package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.response.ConversationResponse;
import org.comunidad.api_integracion_comunitaria.dto.response.MessageResponse;
import org.comunidad.api_integracion_comunitaria.model.*;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final PetitionRepository petitionRepository;

    // --- Métodos Privados Auxiliares ---
    private User getAuthenticatedUser() {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUsername();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // --- 1. Crear o Recuperar Chat ---
    @Transactional
    public ConversationResponse createOrGetConversation(Integer petitionId, Integer targetUserId) {
        User currentUser = getAuthenticatedUser();
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Usuario destino no encontrado"));

        Petition petition = petitionRepository.findById(petitionId)
                .orElseThrow(() -> new RuntimeException("Petición no encontrada"));

        // Verificar si ya existe el chat para no duplicar
        return conversationRepository.findExistingConversation(petitionId, currentUser.getIdUser(), targetUserId)
                .map(c -> mapToConversationResponse(c, currentUser.getIdUser()))
                .orElseGet(() -> {
                    // Si no existe, creamos uno nuevo
                    Conversation conversation = new Conversation();
                    conversation.setPetition(petition);
                    conversation.setCreatedAt(LocalDateTime.now());
                    Conversation savedConv = conversationRepository.save(conversation);

                    // Agregamos participante 1 (Yo)
                    ConversationParticipant p1 = new ConversationParticipant();
                    p1.setConversation(savedConv);
                    p1.setUser(currentUser);
                    participantRepository.save(p1);

                    // Agregamos participante 2 (El otro)
                    ConversationParticipant p2 = new ConversationParticipant();
                    p2.setConversation(savedConv);
                    p2.setUser(targetUser);
                    participantRepository.save(p2);

                    return mapToConversationResponse(savedConv, currentUser.getIdUser());
                });
    }

    // --- 2. Enviar Mensaje ---
    @Transactional
    public MessageResponse sendMessage(Long conversationId, String content) {
        User currentUser = getAuthenticatedUser();

        // Seguridad: Verificar que el usuario pertenece al chat
        if (!participantRepository.existsByConversation_IdConversationAndUser_IdUser(conversationId,
                currentUser.getIdUser())) {
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

        // TODO: Aquí se debería integrar NotificationService para avisar al otro
        // usuario

        return mapToMessageResponse(savedMsg, currentUser.getIdUser());
    }

    // --- 3. Listar mis chats ---
    public List<ConversationResponse> getUserConversations() {
        User currentUser = getAuthenticatedUser();
        return conversationRepository.findByUserId(currentUser.getIdUser()).stream()
                .map(c -> mapToConversationResponse(c, currentUser.getIdUser()))
                .collect(Collectors.toList());
    }

    // --- 4. Ver historial de mensajes (PAGINADO) ---
    // Actualizado para recibir Pageable y devolver Page<MessageResponse>
    public Page<MessageResponse> getConversationMessages(Long conversationId, Pageable pageable) {
        User currentUser = getAuthenticatedUser();

        // Seguridad: Verificar si soy participante
        if (!participantRepository.existsByConversation_IdConversationAndUser_IdUser(conversationId,
                currentUser.getIdUser())) {
            throw new RuntimeException("Acceso denegado a esta conversación.");
        }

        // Llamada al repositorio paginado y mapeo a DTO
        return messageRepository.findByConversation_IdConversation(conversationId, pageable)
                .map(m -> mapToMessageResponse(m, currentUser.getIdUser()));
    }

    // --- Mappers ---
    private ConversationResponse mapToConversationResponse(Conversation c, Integer myUserId) {
        // Nota: Para obtener el nombre real del "otro" usuario, se requeriría una
        // consulta extra
        // o mejorar la entidad Conversation. Por ahora se mantiene la lógica original.
        return ConversationResponse.builder()
                .idConversation(c.getIdConversation())
                .petitionId(c.getPetition().getIdPetition())
                .petitionTitle(c.getPetition().getDescription())
                .otherUserName("Usuario del Chat")
                .build();
    }

    private MessageResponse mapToMessageResponse(Message m, Integer myUserId) {
        return MessageResponse.builder()
                .idMessage(m.getIdMessage())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .senderId(m.getSender().getIdUser())
                .senderName(m.getSender().getName())
                .isMine(m.getSender().getIdUser().equals(myUserId))
                .build();
    }
}