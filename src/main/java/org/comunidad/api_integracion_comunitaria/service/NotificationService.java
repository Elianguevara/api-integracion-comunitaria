package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.comunidad.api_integracion_comunitaria.model.*;
import org.comunidad.api_integracion_comunitaria.repository.NotificationRepository;
import org.comunidad.api_integracion_comunitaria.repository.ProviderRepository;
import org.comunidad.api_integracion_comunitaria.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;

    // --- MÉTODOS DE CREACIÓN ---

    @Transactional
    public void notifyNewPostulation(Petition petition) {
        User clientUser = petition.getCustomer().getUser();
        String title = "Nueva Postulación";
        String message = "Un proveedor ha enviado un presupuesto para: " + petition.getProfession().getName();
        String link = "/petition/" + petition.getIdPetition();
        createNotification(clientUser, title, message, "INFO", link, petition, null);
    }

    @Transactional
    public void notifyPostulationAccepted(Postulation postulation) {
        User providerUser = postulation.getProvider().getUser();
        String title = "¡Presupuesto Aceptado!";
        String message = "Felicidades, han aceptado tu trabajo para: " + postulation.getPetition().getDescription();
        String link = "/feed";
        createNotification(providerUser, title, message, "SUCCESS", link, postulation.getPetition(), postulation);
    }

    @Transactional
    public void notifyPostulationRejected(Postulation postulation) {
        User providerUser = postulation.getProvider().getUser();
        String title = "Postulación Finalizada";
        String message = "El cliente ha seleccionado a otro profesional para: " + postulation.getPetition().getDescription();
        String link = "/feed";
        createNotification(providerUser, title, message, "WARNING", link, postulation.getPetition(), postulation);
    }

    // --- MÉTODOS DE BÚSQUEDA Y LECTURA (LOS QUE FALTABAN) ---

    @Transactional(readOnly = true)
    public Page<Notification> getMyNotifications(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Asegúrate de que NotificationRepository tenga este método definido:
        // Page<Notification> findByUser_IdUserOrderByCreatedAtDesc(Integer userId, Pageable pageable);
        return notificationRepository.findByUser_IdUserOrderByCreatedAtDesc(user.getIdUser(), pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Asegúrate de que NotificationRepository tenga este método:
        // long countByUser_IdUserAndIsReadFalse(Integer userId);
        return notificationRepository.countByUser_IdUserAndIsReadFalse(user.getIdUser());
    }

    @Transactional
    public void markAsRead(Integer idNotification) {
        Notification notification = notificationRepository.findById(idNotification)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!notification.getUser().getEmail().equals(email)) {
            throw new RuntimeException("No tienes permiso");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    // Método auxiliar privado
    private void createNotification(User user, String title, String message, String type, String link, Petition petition, Postulation postulation) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(type);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        if (link != null) notification.setMetadata(link);
        if (petition != null) notification.setRelatedPetition(petition);
        if (postulation != null) notification.setRelatedPostulation(postulation);
        notificationRepository.save(notification);
    }
    /**
     * Notifica a los proveedores que coinciden con la profesión y ciudad de una nueva petición.
     */
    @Transactional
    public void notifyProvidersByProfessionAndCity(Integer idProfession, Integer idCity, Petition petition) {
        // Usamos el repositorio de proveedores para buscar los candidatos
        List<Provider> candidates = providerRepository.findByProfessionAndCity(idProfession, idCity);

        for (Provider provider : candidates) {
            // Evitar notificar al mismo usuario si es quien creó la petición (caso raro pero posible)
            if (!provider.getUser().getIdUser().equals(petition.getCustomer().getUser().getIdUser())) {

                String title = "Nueva Oportunidad Laboral";
                String message = "Se busca " + petition.getProfession().getName() + " en " + petition.getCity().getName();
                String link = "/petition/" + petition.getIdPetition();

                // Reutilizamos el método privado para crear la notificación
                createNotification(
                        provider.getUser(),
                        title,
                        message,
                        "INFO",
                        link,
                        petition,
                        null
                );
            }
        }
    }
}