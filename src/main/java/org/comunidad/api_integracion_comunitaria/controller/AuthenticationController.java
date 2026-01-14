package org.comunidad.api_integracion_comunitaria.controller;

import lombok.RequiredArgsConstructor;

import org.comunidad.api_integracion_comunitaria.dto.request.AuthenticationRequest;
import org.comunidad.api_integracion_comunitaria.dto.request.RegisterRequest;
import org.comunidad.api_integracion_comunitaria.dto.response.AuthenticationResponse;
import org.comunidad.api_integracion_comunitaria.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}