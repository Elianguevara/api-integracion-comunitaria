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
 * <li>Asignación de roles y perfiles específicos.</li>
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
     * <li>Asigna el rol correspondiente (ROLE_USER o ROLE_PROVIDER).</li>
     * <li>Crea el perfil específico ({@link Customer} o {@link Provider}).</li>
     * <li>Genera y retorna un token JWT.</li>
     * </ol>
     * </p>
     *
     * @param request Objeto {@link RegisterRequest} con los datos del formulario de
     *                registro.
     * @return {@link AuthenticationResponse} conteniendo el token JWT generado.
     * @throws RuntimeException Si el email o la combinación de nombre/apellido ya
     *                          existen,
     *                          o si el rol configurado no se encuentra en la base
     *                          de datos.
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
        // Se asume que el frontend envía "PROVIDER" o cualquier otra cosa para cliente.
        final String roleName = "PROVIDER".equalsIgnoreCase(request.getRole()) ? "ROLE_PROVIDER" : "ROLE_USER";

        Role roleEntity = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error interno: Rol no encontrado en BD (" + roleName + ")"));

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(roleEntity);
        userRoleRepository.save(userRole);

        // 4. Crear Perfil Específico (Strategy implícito)
        if ("ROLE_PROVIDER".equals(roleName)) {
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
                .build();
    }

    /**
     * Autentica a un usuario existente.
     * <p>
     * Valida las credenciales contra la base de datos usando
     * {@link AuthenticationManager}.
     * Si son correctas, genera un nuevo token JWT.
     * </p>
     *
     * @param request Objeto {@link AuthenticationRequest} con email y contraseña.
     * @return {@link AuthenticationResponse} con el token JWT.
     * @throws org.springframework.security.core.AuthenticationException Si las
     *                                                                   credenciales
     *                                                                   son
     *                                                                   inválidas.
     * @throws java.util.NoSuchElementException                          Si el
     *                                                                   usuario no
     *                                                                   existe en
     *                                                                   la base de
     *                                                                   datos tras
     *                                                                   la
     *                                                                   autenticación.
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

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Convierte una entidad de dominio {@link User} a un {@link UserDetails} de
     * Spring Security.
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

        // Transformamos los roles al formato esperado por el builder (sin prefijo ROLE_
        // si es necesario,
        // aunque Spring Security suele manejarlo internamente, aquí lo limpiamos por
        // compatibilidad)
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