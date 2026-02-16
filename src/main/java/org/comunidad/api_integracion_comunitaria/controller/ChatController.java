package org.comunidad.api_integracion_comunitaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.MessageRequest;
import org.comunidad.api_integracion_comunitaria.dto.request.StartConversationRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.ConversationResponse;
import org.comunidad.api_integracion_comunitaria.dto.response.MessageResponse;
import org.comunidad.api_integracion_comunitaria.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat/conversations") // Ajustado a la ruta estándar de tu frontend
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 1. Obtener o crear chat
    @PostMapping
    public ResponseEntity<ConversationResponse> startConversation(@Valid @RequestBody StartConversationRequest request) {
        return ResponseEntity.ok(chatService.createOrGetConversation(request.getPetitionId(), request.getProviderId()));
    }

    // 2. Mis conversaciones (Bandeja de entrada)
    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getMyConversations() {
        return ResponseEntity.ok(chatService.getUserConversations());
    }

    // 3. Ver historial de mensajes de un chat específico
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long conversationId) {
        return ResponseEntity.ok(chatService.getConversationMessages(conversationId));
    }

    // 4. Enviar mensaje
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody MessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(conversationId, request.getContent()));
    }

    // 5. Marcar mensajes de una conversación como leídos
    @PutMapping("/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long conversationId) {
        chatService.markMessagesAsRead(conversationId);
        return ResponseEntity.ok().build();
    }
}