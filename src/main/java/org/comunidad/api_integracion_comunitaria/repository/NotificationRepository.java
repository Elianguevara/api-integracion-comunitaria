package org.comunidad.api_integracion_comunitaria.repository;

import org.comunidad.api_integracion_comunitaria.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de datos de las Notificaciones.
 * <p>
 * Incluye métodos optimizados para listar el historial de forma paginada
 * y utilidades para la interfaz de usuario (como contar no leídos).
 * </p>
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    /**
     * Recupera las notificaciones de un usuario específico con soporte de
     * paginación.
     * <p>
     * Se usa en la pantalla de "Mis Notificaciones" para evitar descargar
     * todo el historial de golpe.
     * </p>
     *
     * @param idUser   ID del usuario dueño de las notificaciones.
     * @param pageable Configuración de paginación (ej: página 0, tamaño 20, orden
     *                 DESC por fecha).
     * @return Una página (Page) de notificaciones.
     */
    Page<Notification> findByUser_IdUser(Integer idUser, Pageable pageable);

    /**
     * Cuenta cuántas notificaciones tiene el usuario sin leer.
     * <p>
     * Este método es extremadamente ligero y rápido (hace un SELECT COUNT).
     * Ideal para mostrar el número rojo en la campana de la barra de navegación.
     * </p>
     *
     * @param idUser ID del usuario.
     * @return Número total de notificaciones no leídas (isRead = false).
     */
    long countByUser_IdUserAndIsReadFalse(Integer idUser);
}