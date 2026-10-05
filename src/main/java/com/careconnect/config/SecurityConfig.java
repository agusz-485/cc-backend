package com.careconnect.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":401,\"error\":\"No autorizado\",\"message\":\"Debes iniciar sesión con una cuenta para realizar esta acción.\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":403,\"error\":\"Acceso denegado\",\"message\":\"No tienes permisos para realizar esta acción con tu rol actual.\"}");
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 1. Archivos estáticos y subidas públicas
                .requestMatchers("/api/v1/uploads", "/api/v1/uploads/**").permitAll()
                .requestMatchers("/uploads", "/uploads/**").permitAll()
                // 2. La regla específica de /me exige autenticación con JWT:
                .requestMatchers("/api/v1/auth/me").authenticated()
                // 3. Registro y Login quedan públicos:
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // 4. Permite lectura pública de perfiles, búsqueda en directorio y reseñas
                .requestMatchers(HttpMethod.GET, "/api/v1/cuidadores/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/enfermeros/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/resenias/**").permitAll()
                .requestMatchers("/api/v1/enfermeros/**").hasAnyRole("ENFERMERO", "ADMIN")
                .requestMatchers("/api/v1/cuidadores/**").hasAnyRole("CUIDADOR", "ADMIN")
                .requestMatchers("/api/v1/turnos/**").authenticated()
                .requestMatchers("/api/v1/adultos-mayores/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/servicios/**").authenticated()
                .requestMatchers("/api/v1/resenias/**").authenticated()
                .requestMatchers("/api/v1/reportes/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*", "https://*.vercel.app", "https://*.onrender.com", "*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}