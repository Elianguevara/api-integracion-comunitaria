package org.comunidad.api_integracion_comunitaria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.MessageRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.ConversationResponse;
import org.comunidad.api_integracion_comunitaria.dto.response.MessageResponse;
import org.comunidad.api_integracion_comunitaria.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 1. Iniciar chat (Ej: Proveedor hace clic en "Contactar" en una Petición)
    @PostMapping("/start")
    public ResponseEntity<ConversationResponse> startConversation(
            @RequestParam Integer petitionId,
            @RequestParam Integer targetUserId) {
        return ResponseEntity.ok(chatService.createOrGetConversation(petitionId, targetUserId));
    }

    // 2. Mis conversaciones (Bandeja de entrada)
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getMyConversations() {
        return ResponseEntity.ok(chatService.getUserConversations());
    }

    // 3. Enviar mensaje
    @PostMapping("/messages/{conversationId}")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody MessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(conversationId, request.getContent()));
    }

    // 4. Ver historial
    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long conversationId) {
        return ResponseEntity.ok(chatService.getConversationMessages(conversationId));
    }
}