package com.sanosysalvos.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * El BFF actúa como Resource Server: exige que toda petición traiga un JWT
 * válido, emitido por el tenant de Microsoft Entra ID configurado en
 * bff-service.yml (issuer, audiencia y firma se validan automáticamente
 * gracias a spring-cloud-azure-starter-active-directory).
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Swagger queda abierto para poder revisar la documentación sin token
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        return http.build();
    }
}
