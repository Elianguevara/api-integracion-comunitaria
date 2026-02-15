package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.ProviderProfileRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.ProviderPublicProfileResponse;
import org.comunidad.api_integracion_comunitaria.model.*;
import org.comunidad.api_integracion_comunitaria.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final ProfessionRepository professionRepository;
    private final CityRepository cityRepository;
    private final ProviderCityRepository providerCityRepository;

    // NUEVO: Repositorio para obtener las calificaciones del proveedor
    private final GradeProviderRepository gradeProviderRepository;

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

    // --- NUEVO MÉTODO: Obtener Perfil Público del Proveedor ---
    @Transactional(readOnly = true)
    public ProviderPublicProfileResponse getPublicProfile(Integer idProvider) {
        // 1. Buscamos el proveedor
        Provider provider = providerRepository.findById(idProvider)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado."));

        User user = provider.getUser();

        // 2. Extraemos los nombres de las ciudades donde trabaja
        List<String> cities = provider.getProviderCities().stream()
                .map(pc -> pc.getCity().getName())
                .collect(Collectors.toList());

        // 3. Extraemos la profesión principal (en una lista por flexibilidad futura)
        List<String> professions = provider.getProfession() != null
                ? List.of(provider.getProfession().getName())
                : List.of();

        // 4. Calculamos Calificaciones (Promedio y Total)
        Double avg = gradeProviderRepository.getAverageRating(idProvider);
        Double finalRating = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;

        Integer totalReviews = gradeProviderRepository.countByProvider_IdProvider(idProvider);
        if (totalReviews == null) {
            totalReviews = 0;
        }

        // 5. Construimos el DTO de respuesta
        return ProviderPublicProfileResponse.builder()
                .idProvider(provider.getIdProvider())
                .userId(user.getIdUser())
                .name(user.getName())
                .lastname(user.getLastname())
                .profileImage(user.getProfileImage())
                .biography(provider.getDescription())
                .professions(professions)
                .cities(cities)
                .rating(finalRating)
                .totalReviews(totalReviews)
                .build();
    }
}