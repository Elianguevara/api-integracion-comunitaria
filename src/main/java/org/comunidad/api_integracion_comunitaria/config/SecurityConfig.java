package org.comunidad.api_integracion_comunitaria.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Clase de configuración principal de Spring Security.
 * <p>
 * Define las reglas de seguridad de la aplicación, incluyendo:
 * <ul>
 * <li>Filtros de autenticación (JWT).</li>
 * <li>Políticas de CORS (Cross-Origin Resource Sharing).</li>
 * <li>Rutas públicas y privadas.</li>
 * <li>Gestión de sesiones (Stateless).</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    /**
     * Configura la cadena de filtros de seguridad (Security Filter Chain).
     * <p>
     * Aquí se establece "quién puede entrar a dónde".
     * </p>
     *
     * @param http Objeto HttpSecurity para configurar la seguridad web.
     * @return La cadena de filtros construida.
     * @throws Exception Si ocurre un error en la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF (Cross-Site Request Forgery)
                // No es necesario en APIs REST que usan tokens JWT, ya que no hay sesiones de
                // navegador
                .csrf(csrf -> csrf.disable())

                // 2. Configurar CORS
                // Permite que el frontend (React) haga peticiones a este backend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Configurar autorización de rutas (El "Portero")
                .authorizeHttpRequests(auth -> auth
                        // A. Rutas de Autenticación (Públicas)
                        // Permite registrarse y loguearse sin tener token
                        .requestMatchers("/api/auth/**").permitAll()

                        // B. Rutas de Archivos Estáticos (Imágenes)
                        // Permite ver las fotos de perfil y peticiones sin estar logueado
                        // Importante para que las etiquetas <img src="..."> del front funcionen
                        .requestMatchers("/uploads/**").permitAll()

                        // C. Resto de rutas (Privadas)
                        // Cualquier otra petición requiere un token JWT válido
                        .anyRequest().authenticated())

                // 4. Gestión de Sesiones
                // Indicamos que NO guarde estado en el servidor (Stateless).
                // Cada petición debe traer su propia autenticación (el token).
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 5. Proveedor de Autenticación
                // Conecta con nuestra lógica de base de datos (UserDetailsService)
                .authenticationProvider(authenticationProvider)

                // 6. Filtro JWT
                // Añadimos nuestro filtro personalizado ANTES del filtro estándar de
                // usuario/password.
                // Esto permite interceptar el token y autenticar al usuario antes de llegar al
                // controlador.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuración de CORS (Intercambio de recursos de origen cruzado).
     * <p>
     * Define qué dominios externos pueden consultar esta API.
     * Es fundamental para permitir la conexión desde aplicaciones
     * React/Angular/Mobile.
     * </p>
     *
     * @return Fuente de configuración de CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (Frontend de desarrollo)
        // Ajustar puertos según necesidad (React suele ser 3000 o 5173 con Vite)
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cabeceras permitidas
        // Authorization: Para enviar el Token Bearer
        // Content-Type: Para enviar JSON o archivos (Multipart)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Registrar esta configuración para todas las rutas (/**)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}