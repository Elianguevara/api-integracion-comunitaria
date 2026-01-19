package org.comunidad.api_integracion_comunitaria.controller;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.model.User;
import org.comunidad.api_integracion_comunitaria.service.UserService;
import org.comunidad.api_integracion_comunitaria.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Endpoint para que un usuario elimine su PROPIA cuenta.
     * URL: DELETE /users/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyAccount() {
        // 1. Obtener el usuario autenticado del contexto de seguridad
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Ejecutar la baja lógica
        userService.deleteUser(user.getIdUser());

        return ResponseEntity.ok("Tu cuenta ha sido eliminada exitosamente (Baja lógica).");
    }
}