package org.comunidad.api_integracion_comunitaria.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.comunidad.api_integracion_comunitaria.service.JwtService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // Lo implementaremos en el siguiente paso

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Obtener la cabecera de autorización
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Verificar si la cabecera es válida (debe empezar con "Bearer ")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token
        jwt = authHeader.substring(7); // "Bearer " son 7 caracteres
        userEmail = jwtService.extractUsername(jwt);

        // 4. Si hay email y el usuario no está autenticado todavía
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Cargamos los detalles del usuario desde la base de datos
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Validamos el token
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Creamos el objeto de autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Establecemos la autenticación en el contexto de Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}