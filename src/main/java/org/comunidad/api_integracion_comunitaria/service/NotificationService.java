package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.response.NotificationResponse;
import org.comunidad.api_integracion_comunitaria.model.*;
import org.comunidad.api_integracion_comunitaria.repository.NotificationRepository;
import org.comunidad.api_integracion_comunitaria.repository.ProviderRepository;
import org.comunidad.api_integracion_comunitaria.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;

    /**
     * Envía una notificación a un usuario específico.
     * Método genérico para ser usado por otros servicios (Peticiones,
     * Postulaciones, etc).
     */
    @Transactional
    public void sendNotification(User recipient, String title, String message, String type,
            Petition petition, Postulation postulation) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(type); // Ej: "POSTULATION_ACCEPTED"
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRelatedPetition(petition);
        notification.setRelatedPostulation(postulation);

        notificationRepository.save(notification);
    }

    /**
     * Recupera las notificaciones del usuario autenticado de forma paginada.
     *
     * @param pageable Configuración de la página solicitada (página, tamaño,
     *                 orden).
     * @return Página de respuestas de notificaciones.
     */
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        // 1. Obtener usuario actual
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscar en BD usando paginación
        // Nota: El ordenamiento (OrderByCreatedAtDesc) debe venir configurado en el
        // objeto 'pageable' desde el Controller
        return notificationRepository.findByUser_IdUser(user.getIdUser(), pageable)
                .map(this::mapToResponse);
    }

    /**
     * Cuenta las notificaciones sin leer del usuario actual.
     * Ideal para mostrar el "badge" o contador rojo en la interfaz.
     */
    public long getUnreadCount() {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return notificationRepository.countByUser_IdUserAndIsReadFalse(user.getIdUser());
    }

    /**
     * Marca una notificación específica como leída.
     */
    public void markAsRead(Integer id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        // Opcional: Validar que la notificación pertenezca al usuario actual por
        // seguridad

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    /**
     * Notifica masivamente a proveedores filtrando por Profesión y Ciudad.
     */
    @Transactional
    public void notifyProvidersByProfessionAndCity(Integer idProfession, Integer idCity, Petition petition) {
        // 1. Buscamos a los interesados
        List<Provider> providers = providerRepository.findByProfessionAndCity(idProfession, idCity);

        // 2. Filtramos al propio dueño
        Integer ownerId = petition.getCustomer().getUser().getIdUser();

        // 3. Enviamos masivamente
        for (Provider provider : providers) {
            if (!provider.getUser().getIdUser().equals(ownerId)) {
                String professionName = petition.getProfession().getName();
                String cityName = petition.getCity() != null ? petition.getCity().getName() : "tu zona";

                sendNotification(
                        provider.getUser(),
                        "¡Oportunidad en " + cityName + "!",
                        "Se busca " + professionName + " para: " + petition.getDescription(),
                        "OPPORTUNITY",
                        petition,
                        null);
            }
        }
    }

    /**
     * Convierte Entidad a DTO.
     */
    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getNotificationType())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .relatedPetitionId(n.getRelatedPetition() != null ? n.getRelatedPetition().getIdPetition() : null)
                .relatedPostulationId(
                        n.getRelatedPostulation() != null ? n.getRelatedPostulation().getIdPostulation() : null)
                .build();
    }
}