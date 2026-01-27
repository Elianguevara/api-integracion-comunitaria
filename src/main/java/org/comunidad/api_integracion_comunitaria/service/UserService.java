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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final CustomerRepository customerRepository;
    private final ProfessionRepository professionRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Optional<Provider> providerOpt = providerRepository.findByUser(user);
        Optional<Customer> customerOpt = customerRepository.findByUser(user);

        UserProfileResponse.UserProfileResponseBuilder response = UserProfileResponse.builder()
                .id(user.getIdUser())
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .profileImage(user.getProfileImage());

        if (providerOpt.isPresent()) {
            Provider p = providerOpt.get();
            response.role("PROVIDER");
            response.description(p.getDescription());
            if (p.getProfession() != null) response.profession(p.getProfession().getName());

            response.stats(List.of(
                    new UserProfileResponse.StatDTO("Nivel", "Profesional"),
                    new UserProfileResponse.StatDTO("Trabajos", "0")
            ));
        } else if (customerOpt.isPresent()) {
            Customer c = customerOpt.get();
            response.role("CUSTOMER");
            response.phone(c.getPhone());

            response.stats(List.of(
                    new UserProfileResponse.StatDTO("Actividad", "Alta"),
                    new UserProfileResponse.StatDTO("Peticiones", "0")
            ));
        } else {
            response.role("ADMIN");
            response.stats(List.of());
        }

        return response.build();
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UserProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. Actualizar datos base (Tabla User)
        if (request.getName() != null) user.setName(request.getName());
        if (request.getLastname() != null) user.setLastname(request.getLastname());
        userRepository.save(user);

        // 2. Actualizar datos específicos (Tabla Provider o Customer)
        Optional<Provider> providerOpt = providerRepository.findByUser(user);
        Optional<Customer> customerOpt = customerRepository.findByUser(user);

        if (providerOpt.isPresent()) {
            Provider p = providerOpt.get();

            if (request.getDescription() != null) {
                p.setDescription(request.getDescription());
            }

            if (request.getIdProfession() != null) {
                Profession profession = professionRepository.findById(request.getIdProfession())
                        .orElseThrow(() -> new RuntimeException("Profesión inválida"));
                p.setProfession(profession);
            }

            providerRepository.save(p);

        } else if (customerOpt.isPresent()) {
            Customer c = customerOpt.get();

            if (request.getPhone() != null) {
                c.setPhone(request.getPhone());
            }

            customerRepository.save(c);
        }

        // 3. Devolver el perfil actualizado
        return getMyProfile(email);
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setIsActive(false);
        user.setEnabled(false);
        userRepository.save(user);
    }
}