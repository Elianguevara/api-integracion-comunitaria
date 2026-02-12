package org.comunidad.api_integracion_comunitaria.controller;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.response.NotificationResponse;
import org.comunidad.api_integracion_comunitaria.model.Notification;
import org.comunidad.api_integracion_comunitaria.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Obtiene el historial de notificaciones del usuario autenticado.
     * Convierte las Entidades 'Notification' a DTOs 'NotificationResponse'.
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        // 1. Obtener página de Entidades desde el servicio
        Page<Notification> entityPage = notificationService.getMyNotifications(pageable);

        // 2. Mapear Entidad -> DTO
        Page<NotificationResponse> responsePage = entityPage.map(this::mapToResponse);

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Obtiene la cantidad de notificaciones no leídas (Badge rojo).
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    /**
     * Marca una notificación específica como leída.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // --- MAPPER AUXILIAR ---
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                // Ajusta los getters según tu entidad real (getType vs getNotificationType)
                .type(notification.getNotificationType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                // Usamos metadata como link si no existe columna 'link'
                .link(notification.getMetadata())
                .build();
    }
}