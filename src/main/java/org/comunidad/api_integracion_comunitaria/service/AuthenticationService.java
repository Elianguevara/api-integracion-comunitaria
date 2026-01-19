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
 * <p>
 * Maneja la lógica de negocio para:
 * <ul>
 * <li>Registro de nuevos usuarios (Clientes y Proveedores).</li>
 * <li>Autenticación (Login) y generación de tokens JWT.</li>
 * <li>Identificación y retorno del rol del usuario para la navegación en el frontend.</li>
 * </ul>
 * </p>
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

    /**
     * Registra un nuevo usuario en el sistema.
     * <p>
     * Realiza las siguientes operaciones:
     * <ol>
     * <li>Verifica que el email y el nombre/apellido no existan previamente.</li>
     * <li>Crea la entidad {@link User} base con contraseña encriptada.</li>
     * <li>Asigna el rol correspondiente en BD (ROLE_USER o ROLE_PROVIDER).</li>
     * <li>Crea el perfil específico ({@link Customer} o {@link Provider}).</li>
     * <li>Genera un token JWT y determina el rol formateado para el frontend.</li>
     * </ol>
     * </p>
     *
     * @param request Objeto {@link RegisterRequest} con los datos del formulario de registro.
     * @return {@link AuthenticationResponse} conteniendo el token JWT y el rol del usuario.
     * @throws RuntimeException Si el email o la combinación de nombre/apellido ya existen,
     * o si el rol configurado no se encuentra en la base de datos.
     */
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // 1. Validaciones Previas (Evitar duplicados)
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
        // Determinamos el nombre del Rol interno (BD) y externo (Frontend)
        boolean isProvider = "PROVIDER".equalsIgnoreCase(request.getRole());
        final String dbRoleName = isProvider ? "ROLE_PROVIDER" : "ROLE_USER";
        final String frontendRole = isProvider ? "PROVIDER" : "CUSTOMER";

        Role roleEntity = roleRepository.findByName(dbRoleName)
                .orElseThrow(() -> new RuntimeException("Error interno: Rol no encontrado en BD (" + dbRoleName + ")"));

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(roleEntity);
        userRoleRepository.save(userRole);

        // 4. Crear Perfil Específico (Strategy implícito)
        if (isProvider) {
            Provider provider = new Provider();
            provider.setUser(savedUser);
            // El proveedor se crea con perfil vacío, pendiente de completar
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
                .role(frontendRole) // Devolvemos el rol limpio para redirección inmediata
                .name(savedUser.getName() + " " + savedUser.getLastname())
                .email(savedUser.getEmail())
                .build();
    }

    /**
     * Autentica a un usuario existente.
     * <p>
     * Valida las credenciales contra la base de datos usando {@link AuthenticationManager}.
     * Si son correctas:
     * <ol>
     * <li>Genera un nuevo token JWT.</li>
     * <li>Recupera el rol del usuario desde la base de datos.</li>
     * <li>Mapea el rol interno (ej: ROLE_USER) a formato frontend (ej: CUSTOMER).</li>
     * </ol>
     * </p>
     *
     * @param request Objeto {@link AuthenticationRequest} con email y contraseña.
     * @return {@link AuthenticationResponse} con el token JWT y el rol detectado.
     * @throws org.springframework.security.core.AuthenticationException Si las credenciales son inválidas.
     * @throws RuntimeException Si el usuario no existe tras la autenticación (inconsistencia de datos).
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Este método lanzará BadCredentialsException si falla el login
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        // Recuperamos el usuario para generar el token
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado tras autenticación exitosa."));

        var jwtToken = jwtService.generateToken(mapToUserDetails(user));

        // Recuperamos el rol "limpio" para que el frontend sepa a dónde redirigir
        String roleName = getFrontendRoleNameByUser(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(roleName)
                .name(user.getName() + " " + user.getLastname())
                .email(user.getEmail())
                .build();
    }

    /**
     * Método auxiliar para obtener el rol en formato compatible con el Frontend.
     * <p>
     * Traduce los roles de Spring Security (ROLE_PROVIDER, ROLE_USER) a los
     * tipos de usuario de la aplicación (PROVIDER, CUSTOMER).
     * </p>
     * * @param user El usuario del cual extraer el rol.
     * @return Cadena con el rol ("PROVIDER" o "CUSTOMER").
     */
    private String getFrontendRoleNameByUser(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUser_IdUser(user.getIdUser());

        // Fallback por defecto si no tiene roles asignados
        if (userRoles.isEmpty()) return "CUSTOMER";

        // Asumimos que el usuario tiene un rol principal. Tomamos el primero.
        String dbRole = userRoles.get(0).getRole().getName();

        if ("ROLE_PROVIDER".equalsIgnoreCase(dbRole)) {
            return "PROVIDER";
        } else {
            // ROLE_USER u otros se tratan como Cliente
            return "CUSTOMER";
        }
    }

    /**
     * Convierte una entidad de dominio {@link User} a un {@link UserDetails} de Spring Security.
     * <p>
     * Este método es crucial para que JWTService pueda leer los roles y permisos
     * del usuario al momento de crear el token.
     * </p>
     *
     * @param user La entidad de usuario de la base de datos.
     * @return Objeto UserDetails compatible con Spring Security.
     */
    private UserDetails mapToUserDetails(User user) {

        // Buscamos los roles en la tabla intermedia (UserRole)
        List<UserRole> userRoles = userRoleRepository.findByUser_IdUser(user.getIdUser());

        // Transformamos los roles al formato esperado por el builder (sin prefijo ROLE_)
        String[] roles = userRoles.stream()
                .map(ur -> ur.getRole().getName().replace("ROLE_", ""))
                .toArray(String[]::new);

        // Fallback de seguridad: si no tiene roles, asignamos USER
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