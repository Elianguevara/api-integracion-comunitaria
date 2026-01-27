package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.AuthenticationRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.AuthenticationResponse;
import org.comunidad.api_integracion_comunitaria.dto.request.RegisterRequest;
import org.comunidad.api_integracion_comunitaria.model.*;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio encargado de la gestión de autenticación y registro de usuarios.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProviderRepository providerRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // 1. Validaciones Previas
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está registrado en el sistema.");
        }

        if (userRepository.existsByNameAndLastname(request.getName(), request.getLastname())) {
            throw new RuntimeException("Ya existe un usuario registrado con ese Nombre y Apellido.");
        }

        // 2. Crear Usuario Base
        var user = new User();
        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setDateCreate(LocalDateTime.now());
        user.setEnabled(true);
        user.setIsActive(true);
        user.setIsStaff(false);
        user.setIsSuperuser(false);

        User savedUser = userRepository.save(user);

        // 3. Determinar y Asignar Rol
        boolean isProvider = "PROVIDER".equalsIgnoreCase(request.getRole());
        final String dbRoleName = isProvider ? "ROLE_PROVIDER" : "ROLE_USER";
        final String frontendRole = isProvider ? "PROVIDER" : "CUSTOMER";

        Role roleEntity = roleRepository.findByName(dbRoleName)
                .orElseThrow(() -> new RuntimeException("Error interno: Rol no encontrado en BD (" + dbRoleName + ")"));

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(roleEntity);
        userRoleRepository.save(userRole);

        // 4. Crear Perfil Específico
        if (isProvider) {
            Provider provider = new Provider();
            provider.setUser(savedUser);
            providerRepository.save(provider);
        } else {
            Customer customer = new Customer();
            customer.setUser(savedUser);
            customerRepository.save(customer);
        }

        // 5. Generar Token JWT
        var jwtToken = jwtService.generateToken(mapToUserDetails(savedUser));

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(frontendRole)
                .name(savedUser.getName() + " " + savedUser.getLastname())
                .email(savedUser.getEmail())
                .build();
    }

    /**
     * Autentica a un usuario existente.
     * CORRECCIÓN IMPORTANTE: Se agrega @Transactional para evitar LazyInitializationException
     * al acceder a los roles del usuario.
     */
    @Transactional // <--- ESTO ES LO QUE FALTABA
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // 1. Validar credenciales (lanza excepción si falla)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        // 2. Recuperar usuario
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado tras autenticación exitosa."));

        // 3. Generar token (Aquí se accede a los Roles Lazy)
        var jwtToken = jwtService.generateToken(mapToUserDetails(user));

        // 4. Obtener rol para el frontend
        String roleName = getFrontendRoleNameByUser(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(roleName)
                .name(user.getName() + " " + user.getLastname())
                .email(user.getEmail())
                .build();
    }

    private String getFrontendRoleNameByUser(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUser_IdUser(user.getIdUser());

        if (userRoles.isEmpty()) return "CUSTOMER";

        // Al acceder a .getRole().getName(), necesitamos que la transacción siga activa
        String dbRole = userRoles.get(0).getRole().getName();

        if ("ROLE_PROVIDER".equalsIgnoreCase(dbRole)) {
            return "PROVIDER";
        } else {
            return "CUSTOMER";
        }
    }

    private UserDetails mapToUserDetails(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUser_IdUser(user.getIdUser());

        // Transformamos los roles (Acceso Lazy a ur.getRole())
        String[] roles = userRoles.stream()
                .map(ur -> ur.getRole().getName().replace("ROLE_", ""))
                .toArray(String[]::new);

        if (roles.length == 0) {
            roles = new String[] { "USER" };
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(roles)
                .build();
    }
}