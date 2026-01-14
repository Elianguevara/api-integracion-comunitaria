package org.comunidad.api_integracion_comunitaria.service;

import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.dto.request.AuthenticationRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.AuthenticationResponse;
import org.comunidad.api_integracion_comunitaria.dto.request.RegisterRequest;
import org.comunidad.api_integracion_comunitaria.model.User;
import org.comunidad.api_integracion_comunitaria.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = new User();
        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Valores por defecto
        user.setDateCreate(LocalDateTime.now());
        user.setEnabled(true);
        user.setIsActive(true);
        user.setIsStaff(false);
        user.setIsSuperuser(false);

        userRepository.save(user);

        // Generamos el token para que el usuario quede logueado inmediatamente
        // Adaptamos tu User a UserDetails
        var jwtToken = jwtService.generateToken(mapToUserDetails(user));

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

    // Método auxiliar para convertir tu entidad User a UserDetails de Spring
    // Security
    // Esto es necesario porque tu entidad User no implementa UserDetails
    // directamente
    private org.springframework.security.core.userdetails.UserDetails mapToUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER") // Hardcodeado por ahora
                .build();
    }
}