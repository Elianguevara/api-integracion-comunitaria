package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.model.User;
import org.comunidad.api_integracion_comunitaria.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void deleteUser(int userId) {
        // 1. Buscamos al usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. BAJA LÓGICA (Soft Delete)
        // No lo borramos, solo lo desactivamos para que no pueda loguearse más
        user.setIsActive(false);
        user.setEnabled(false); // Spring Security usa esto para bloquear el login

        // 3. Guardamos el cambio
        userRepository.save(user);
    }
}