package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.AuthenticationRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.AuthenticationResponse;
import org.comunidad.api_integracion_comunitaria.dto.request.RegisterRequest;
import org.comunidad.api_integracion_comunitaria.model.*; // Importamos todas las entidades
import org.comunidad.api_integracion_comunitaria.repository.*; // Importamos todos los repos
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProviderRepository providerRepository; // <--- Nuevo
    private final RoleRepository roleRepository; // <--- Nuevo
    private final UserRoleRepository userRoleRepository; // <--- Nuevo

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // 1. Crear Usuario base
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

        // 2. Determinar Rol (CORREGIDO: Usamos 'final' y asignamos de una sola vez)
        // Esto soluciona el error "local variable defined in an enclosing scope must be
        // final"
        final String roleName = "PROVIDER".equalsIgnoreCase(request.getRole()) ? "ROLE_PROVIDER" : "ROLE_USER";

        // 3. Buscar el Rol en BD y asignarlo
        Role roleEntity = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado en BD (" + roleName + ")"));

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(roleEntity);
        userRoleRepository.save(userRole);

        // 4. Crear Perfil Específico (Cliente o Proveedor)
        if ("ROLE_PROVIDER".equals(roleName)) {
            Provider provider = new Provider();
            provider.setUser(savedUser);
            // El proveedor se crea en blanco, luego completará perfil
            providerRepository.save(provider);
        } else {
            Customer customer = new Customer();
            customer.setUser(savedUser);
            customerRepository.save(customer);
        }

        // 5. Generar Token
        var jwtToken = jwtService.generateToken(mapToUserDetails(savedUser));

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        var jwtToken = jwtService.generateToken(mapToUserDetails(user));

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    // Método auxiliar actualizado para leer roles reales de la BD
    private org.springframework.security.core.userdetails.UserDetails mapToUserDetails(User user) {

        // Buscamos los roles en la tabla intermedia
        List<UserRole> userRoles = userRoleRepository.findByUser_IdUser(user.getIdUser());

        String[] roles = userRoles.stream()
                .map(ur -> ur.getRole().getName().replace("ROLE_", "")) // Spring espera "USER", no "ROLE_USER" en
                                                                        // builder
                .toArray(String[]::new);

        // Si no tiene roles, asignamos USER por defecto para evitar errores
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