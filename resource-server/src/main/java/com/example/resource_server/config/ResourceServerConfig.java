package com.example.resource_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ResourceServerConfig {

        @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
        private String issuerUri;

        private final CustomJwtAuthenticationConverter jwtAuthenticationConverter;

        public ResourceServerConfig(CustomJwtAuthenticationConverter jwtAuthenticationConverter) {
                this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                                                .requestMatchers("/h2-console/**").permitAll()
                                                .requestMatchers("/api/accounts/**").hasAuthority("SCOPE_accounts")
                                                .requestMatchers("/api/transactions/**")
                                                .hasAuthority("SCOPE_transactions")
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                                .sessionManagement(sessionManagement -> sessionManagement
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                                .headers(headers -> headers.frameOptions().sameOrigin());

                return http.build();
        }
}