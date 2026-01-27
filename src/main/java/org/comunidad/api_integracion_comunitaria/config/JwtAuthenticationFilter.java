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
import io.jsonwebtoken.ExpiredJwtException; // <--- IMPORTANTE: Necesario para manejar el error

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Si no hay cabecera o no empieza con Bearer, pasamos al siguiente filtro sin hacer nada.
        // Esto permite que las rutas públicas funcionen sin token.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Intentamos procesar el token dentro de un bloque try-catch
        try {
            jwt = authHeader.substring(7); // "Bearer " son 7 caracteres

            // AQUÍ es donde fallaba antes si el token estaba vencido
            userEmail = jwtService.extractUsername(jwt);

            // 3. Si extrajimos el email y no estamos autenticados aún...
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Cargamos los detalles del usuario
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 4. Validamos si el token es correcto y pertenece al usuario
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // 5. Marcamos al usuario como autenticado en el sistema
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (ExpiredJwtException e) {
            // MANEJO DE ERROR: El token expiró.
            // No hacemos nada grave, solo lo logueamos. La petición continúa "como anónimo".
            // Si intenta entrar a una ruta protegida, SecurityConfig lo bloqueará con un 403.
            System.out.println("ADVERTENCIA: Token expirado recibido. Continuando como invitado. Error: " + e.getMessage());

        } catch (Exception e) {
            // MANEJO DE ERROR: Token malformado, firma inválida, etc.
            System.out.println("ERROR: Token JWT inválido. Error: " + e.getMessage());
        }

        // 6. SIEMPRE continuamos con la cadena de filtros.
        // Si el token era válido, el SecurityContext ya tiene la autenticación.
        // Si falló (catch), el SecurityContext está vacío y actuará como usuario anónimo.
        filterChain.doFilter(request, response);
    }
}