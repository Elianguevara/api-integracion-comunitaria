package org.comunidad.api_integracion_comunitaria.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.UserProfileRequest; // <--- Importante
import org.comunidad.api_integracion_comunitaria.dto.response.UserProfileResponse; // <--- Importante
import org.comunidad.api_integracion_comunitaria.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users") // <--- Ruta correcta
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "Operaciones de gestión de perfil de usuario")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Obtener mi perfil", description = "Devuelve info del usuario y su rol.")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(userService.getMyProfile(auth.getName()));
    }

    @Operation(summary = "Actualizar perfil", description = "Modifica datos base y específicos del rol.")
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyUser(@RequestBody UserProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Llamamos al método completo (updateProfile), no al básico
        return ResponseEntity.ok(userService.updateProfile(auth.getName(), request));
    }

    @Operation(summary = "Eliminar cuenta", description = "Realiza una baja lógica de la cuenta actual.")
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        userService.deleteUserByEmail(auth.getName());
        return ResponseEntity.ok("Tu cuenta ha sido eliminada exitosamente (Baja lógica).");
    }
}