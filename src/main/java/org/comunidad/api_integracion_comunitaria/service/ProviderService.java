package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.ProviderProfileRequest;
import org.comunidad.api_integracion_comunitaria.model.*;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final ProfessionRepository professionRepository;
    private final CityRepository cityRepository;
    private final ProviderCityRepository providerCityRepository;

    @Transactional
    public void updateProfile(ProviderProfileRequest request) {
        // 1. Obtener el usuario autenticado
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscar la entidad Provider asociada
        Provider provider = providerRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new RuntimeException("Perfil de proveedor no encontrado."));

        // 3. Actualizar datos básicos
        Profession profession = professionRepository.findById(request.getIdProfession())
                .orElseThrow(() -> new RuntimeException("Profesión no válida"));

        provider.setProfession(profession);
        provider.setDescription(request.getDescription());

        // Guardamos cambios del proveedor
        providerRepository.save(provider);

        // 4. Actualizar Ciudades de Cobertura
        // Primero borramos las anteriores para evitar duplicados o datos viejos
        providerCityRepository.deleteByProvider_IdProvider(provider.getIdProvider());

        // Insertamos las nuevas
        if (request.getCityIds() != null) {
            for (Integer cityId : request.getCityIds()) {
                City city = cityRepository.findById(cityId)
                        .orElseThrow(() -> new RuntimeException("Ciudad no encontrada: " + cityId));

                ProviderCity providerCity = new ProviderCity();
                providerCity.setProvider(provider);
                providerCity.setCity(city);

                // Configurar auditoría si es necesario (o dejar que JPA lo haga si tienes
                // listeners)
                // providerCity.setDateCreate(LocalDateTime.now());

                providerCityRepository.save(providerCity);
            }
        }
    }
}