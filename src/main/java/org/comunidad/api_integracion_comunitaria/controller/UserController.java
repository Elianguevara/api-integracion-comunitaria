package org.comunidad.api_integracion_comunitaria.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.model.User;
import org.comunidad.api_integracion_comunitaria.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de usuarios.
 * <p>
 * Este controlador maneja las operaciones relacionadas con la cuenta del usuario autenticado,
 * delegando la lógica de negocio a {@link UserService}.
 * </p>
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "Operaciones de gestión de perfil de usuario")
public class UserController {

    private final UserService userService;

    /**
     * Actualiza la información básica del usuario autenticado.
     * <p>
     * Obtiene el usuario actual del contexto de seguridad y actualiza sus datos
     * permitidos (Nombre, Apellido, etc.) a través del servicio.
     * </p>
     *
     * @param userUpdates Objeto {@link User} con los datos a modificar.
     * @return {@link ResponseEntity} con el usuario actualizado y estado HTTP 200 OK.
     */
    @Operation(summary = "Actualizar perfil", description = "Modifica nombre y apellido del usuario logueado.")
    @PutMapping("/me")
    public ResponseEntity<User> updateMyUser(@RequestBody User userUpdates) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User updatedUser = userService.updateBasicInfo(email, userUpdates);

        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Elimina la cuenta del usuario autenticado (Baja Lógica).
     * <p>
     * No elimina el registro físicamente de la base de datos, sino que cambia su estado
     * a inactivo para prevenir futuros inicios de sesión.
     * </p>
     *
     * @return {@link ResponseEntity} con un mensaje de confirmación.
     */
    @Operation(summary = "Eliminar cuenta", description = "Realiza una baja lógica de la cuenta actual.")
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // Delegamos la búsqueda y eliminación al servicio
        userService.deleteUserByEmail(email);

        return ResponseEntity.ok("Tu cuenta ha sido eliminada exitosamente (Baja lógica).");
    }
}