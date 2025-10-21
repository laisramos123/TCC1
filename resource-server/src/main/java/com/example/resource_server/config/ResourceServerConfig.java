package com.example.resource_server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.resource_server.security.ConsentValidationFilter;
import com.example.resource_server.security.JwtAuthenticationConverter;
import com.example.resource_server.service.*;

/**
 * FASE 3: Configuração do Resource Server
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig {

        @Autowired
        private ConsentValidationService consentValidationService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/actuator/health", "/error").permitAll()
                                                .anyRequest().authenticated())

                                // Configuração como Resource Server
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter())))

                                // Adiciona filtro de validação de consentimento
                                .addFilterAfter(
                                                new ConsentValidationFilter(consentValidationService),
                                                UsernamePasswordAuthenticationFilter.class)

                                // Stateless (sem sessão)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .csrf(csrf -> csrf.disable());

                return http.build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                return new JwtAuthenticationConverter();
        }
}