package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.response.NotificationResponse;
import org.comunidad.api_integracion_comunitaria.model.Notification;
import org.comunidad.api_integracion_comunitaria.model.Petition;
import org.comunidad.api_integracion_comunitaria.model.Postulation;
import org.comunidad.api_integracion_comunitaria.model.Provider;
import org.comunidad.api_integracion_comunitaria.model.User;
import org.comunidad.api_integracion_comunitaria.repository.NotificationRepository;
import org.comunidad.api_integracion_comunitaria.repository.ProviderRepository;
import org.comunidad.api_integracion_comunitaria.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;

    // Método genérico para enviar notificaciones (Lo usaremos desde otros
    // servicios)
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

    // Obtener mis notificaciones
    public List<NotificationResponse> getMyNotifications() {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUsername();
        User user = userRepository.findByEmail(email).orElseThrow();

        return notificationRepository.findByUser_IdUserOrderByCreatedAtDesc(user.getIdUser()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Marcar como leída
    public void markAsRead(Integer id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    /**
     * CORRECCIÓN PRINCIPAL:
     * Este método ahora busca proveedores filtrando por Profesión Y Ciudad.
     */
    @Transactional
    public void notifyProvidersByProfessionAndCity(Integer idProfession, Integer idCity, Petition petition) {
        // 1. Buscamos a los interesados usando la query optimizada (Punto 2 del plan)
        List<Provider> providers = providerRepository.findByProfessionAndCity(idProfession, idCity);

        // 2. Filtramos al propio dueño (por si un plomero pide un plomero y se
        // auto-notifica)
        Integer ownerId = petition.getCustomer().getUser().getIdUser();

        // 3. Enviamos masivamente
        for (Provider provider : providers) {
            // Verificamos que no sea el mismo usuario que creó la petición
            if (!provider.getUser().getIdUser().equals(ownerId)) {

                // Construimos un mensaje más personalizado
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