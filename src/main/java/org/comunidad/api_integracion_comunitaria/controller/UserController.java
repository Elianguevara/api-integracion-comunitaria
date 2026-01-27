package org.comunidad.api_integracion_comunitaria.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.UserProfileRequest; // <--- IMPORTANTE
import org.comunidad.api_integracion_comunitaria.dto.response.UserProfileResponse; // <--- IMPORTANTE
import org.comunidad.api_integracion_comunitaria.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "Operaciones de gestión de perfil de usuario")
public class UserController {

    private final UserService userService;

    // --- NUEVO ENDPOINT GET ---
    @Operation(summary = "Obtener mi perfil", description = "Devuelve toda la info del usuario (rol, datos específicos, stats).")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(userService.getMyProfile(auth.getName()));
    }

    // --- ENDPOINT PUT ACTUALIZADO ---
    @Operation(summary = "Actualizar perfil", description = "Modifica datos base y específicos del rol (teléfono, descripción, etc).")
    @PutMapping("/me")
    // Ahora recibimos UserProfileRequest en lugar de User
    public ResponseEntity<UserProfileResponse> updateMyUser(@RequestBody UserProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // Llamamos al nuevo método 'updateProfile' del servicio
        UserProfileResponse updatedUser = userService.updateProfile(email, request);

        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Eliminar cuenta", description = "Realiza una baja lógica de la cuenta actual.")
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        userService.deleteUserByEmail(email);
        return ResponseEntity.ok("Tu cuenta ha sido eliminada exitosamente (Baja lógica).");
    }
}