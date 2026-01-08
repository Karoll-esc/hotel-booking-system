package com.sofka.hotel_booking_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad para desarrollo.
 * 
 * <p>Esta configuración deshabilita la autenticación para permitir 
 * el acceso sin restricciones durante el desarrollo.</p>
 * 
 * <p><strong>IMPORTANTE:</strong> Esta configuración es solo para desarrollo.
 * En producción se debe implementar OAuth2 + JWT.</p>
 * 
 * @author Sistema Hotel Booking
 * @version 1.0
 * @since 2026-01-07
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad.
     * Permite todas las peticiones sin autenticación.
     * 
     * @param http el objeto HttpSecurity para configurar
     * @return la cadena de filtros configurada
     * @throws Exception si hay error en la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
