package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.UserProfileRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.UserProfileResponse;
import org.comunidad.api_integracion_comunitaria.model.Customer;
import org.comunidad.api_integracion_comunitaria.model.Profession;
import org.comunidad.api_integracion_comunitaria.model.Provider;
import org.comunidad.api_integracion_comunitaria.model.User;
import org.comunidad.api_integracion_comunitaria.repository.CustomerRepository;
import org.comunidad.api_integracion_comunitaria.repository.ProfessionRepository;
import org.comunidad.api_integracion_comunitaria.repository.ProviderRepository;
import org.comunidad.api_integracion_comunitaria.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio encargado de la lógica de negocio relacionada con los Usuarios.
 * <p>
 * Maneja la orquestación entre la entidad base {@link User} y sus roles específicos
 * {@link Provider} y {@link Customer}, permitiendo operaciones unificadas de lectura y escritura.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final CustomerRepository customerRepository;
    private final ProfessionRepository professionRepository; // Necesario para actualizar profesión

    /**
     * Recupera el perfil completo del usuario autenticado.
     * <p>
     * Este método busca al usuario base y luego consulta los repositorios de roles
     * (Provider/Customer) para ensamblar un objeto de respuesta unificado {@link UserProfileResponse}.
     * </p>
     *
     * @param email El correo electrónico del usuario autenticado (extraído del token).
     * @return Un objeto {@link UserProfileResponse} con toda la información del perfil combinada.
     * @throws RuntimeException Si el usuario no existe en la base de datos.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String email) {
        // 1. Buscar usuario base
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

        // 2. Determinar si es Proveedor o Cliente buscando en las tablas respectivas
        Optional<Provider> providerOpt = providerRepository.findByUser(user);
        Optional<Customer> customerOpt = customerRepository.findByUser(user);

        // 3. Construir respuesta base
        UserProfileResponse.UserProfileResponseBuilder response = UserProfileResponse.builder()
                .id(user.getIdUser())
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .profileImage(user.getProfileImage());

        // 4. Completar datos específicos según el rol
        if (providerOpt.isPresent()) {
            Provider p = providerOpt.get();
            response.role("PROVIDER");
            response.description(p.getDescription());

            if (p.getProfession() != null) {
                response.profession(p.getProfession().getName());
            }

            // TODO: Calcular estadísticas reales desde repositorios de Reviews/Trabajos
            response.stats(List.of(
                    new UserProfileResponse.StatDTO("Nivel", "Profesional"),
                    new UserProfileResponse.StatDTO("Trabajos", "0") // Placeholder
            ));
        } else if (customerOpt.isPresent()) {
            Customer c = customerOpt.get();
            response.role("CUSTOMER");
            response.phone(c.getPhone());

            // TODO: Calcular estadísticas reales de actividad
            response.stats(List.of(
                    new UserProfileResponse.StatDTO("Actividad", "Alta"),
                    new UserProfileResponse.StatDTO("Peticiones", "0") // Placeholder
            ));
        } else {
            response.role("ADMIN"); // O usuario sin rol específico
            response.stats(List.of());
        }

        return response.build();
    }

    /**
     * Actualiza la información del perfil del usuario y sus datos específicos de rol.
     * <p>
     * Este método actualiza la tabla `User` (nombre, apellido) y, dependiendo del rol detectado,
     * actualiza también la tabla `Provider` (descripción, profesión) o `Customer` (teléfono).
     * </p>
     *
     * @param email   El correo del usuario autenticado.
     * @param request DTO con los datos a actualizar (contiene campos de usuario y de rol).
     * @return El perfil actualizado invocando internamente a {@link #getMyProfile(String)}.
     */
    @Transactional
    public UserProfileResponse updateProfile(String email, UserProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. Actualizar datos base (Tabla User)
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getLastname() != null && !request.getLastname().isBlank()) {
            user.setLastname(request.getLastname());
        }
        // Nota: La imagen de perfil se podría manejar aquí si viene en el request
        // if (request.getProfileImage() != null) user.setProfileImage(request.getProfileImage());

        userRepository.save(user);

        // 2. Actualizar datos específicos (Tabla Provider o Customer)
        Optional<Provider> providerOpt = providerRepository.findByUser(user);
        Optional<Customer> customerOpt = customerRepository.findByUser(user);

        if (providerOpt.isPresent()) {
            Provider provider = providerOpt.get();

            // Actualizar Descripción
            if (request.getDescription() != null) {
                provider.setDescription(request.getDescription());
            }

            // Actualizar Profesión (Si viene el ID en el request)
            if (request.getIdProfession() != null) {
                Profession profession = professionRepository.findById(request.getIdProfession())
                        .orElseThrow(() -> new RuntimeException("Profesión no válida"));
                provider.setProfession(profession);
            }

            providerRepository.save(provider);

        } else if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();

            // Actualizar Teléfono
            if (request.getPhone() != null) {
                customer.setPhone(request.getPhone());
            }

            customerRepository.save(customer);
        }

        // 3. Devolver el perfil actualizado refrescando los datos
        return getMyProfile(email);
    }

    /**
     * Realiza una baja lógica del usuario basada en su correo electrónico.
     * <p>
     * Busca al usuario por email y delega la operación al método {@link #deleteUser(int)}.
     * </p>
     *
     * @param email El correo del usuario a eliminar.
     */
    @Transactional
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
        deleteUser(user.getIdUser());
    }

    /**
     * Método interno para ejecutar la baja lógica (Soft Delete).
     * <p>
     * Establece {@code isActive} y {@code enabled} en falso para impedir el acceso,
     * pero mantiene la integridad referencial de los datos históricos.
     * </p>
     *
     * @param userId El ID numérico del usuario.
     */
    @Transactional
    public void deleteUser(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Baja lógica
        user.setIsActive(false);
        user.setEnabled(false); // Bloqueo a nivel de Spring Security

        userRepository.save(user);
    }
}