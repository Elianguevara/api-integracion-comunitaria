package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Traer notificaciones del usuario ordenadas por las más recientes
    List<Notification> findByUser_IdUserOrderByCreatedAtDesc(Integer idUser);

    // Contar cuántas tiene sin leer (útil para el numerito rojo en el icono de
    // campana)
    long countByUser_IdUserAndIsReadFalse(Integer idUser);
}