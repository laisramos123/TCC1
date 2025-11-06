package com.example.resource_server.config;

import com.example.resource_server.jwt.CustomJwtDecoder;
import com.example.resource_server.security.ConsentValidationFilter;
import com.example.resource_server.security.JwtAuthenticationConverter;
import com.example.resource_server.service.ConsentValidationService;
import com.example.resource_server.signature.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig {

        @Value("${jwt.signature.algorithm:RSA}")
        private String algorithmName;

        private final ConsentValidationService consentValidationService;
        private final SignatureAlgorithm rsaSignature;
        private final SignatureAlgorithm dilithiumSignature;

        public ResourceServerConfig(
                        ConsentValidationService consentValidationService,
                        @Qualifier("rsaSignature") SignatureAlgorithm rsaSignature,
                        @Qualifier("dilithiumSignature") SignatureAlgorithm dilithiumSignature) {

                this.consentValidationService = consentValidationService;
                this.rsaSignature = rsaSignature;
                this.dilithiumSignature = dilithiumSignature;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/actuator/health", "/error").permitAll()
                                                .anyRequest().authenticated())

                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter())
                                                                .decoder(jwtDecoder())))

                                .addFilterAfter(
                                                new ConsentValidationFilter(consentValidationService),
                                                UsernamePasswordAuthenticationFilter.class)

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .csrf(csrf -> csrf.disable());

                return http.build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                return new JwtAuthenticationConverter();
        }

        @Bean
        public JwtDecoder jwtDecoder() {
                try {
                        SignatureAlgorithm algorithm = getActiveAlgorithm();

                        if (algorithm.getPublicKey() == null) {
                                algorithm.generateKeyPair();
                        }

                        return new CustomJwtDecoder(algorithm);

                } catch (Exception e) {
                        throw new RuntimeException("Failed to create JWT decoder", e);
                }
        }

        private SignatureAlgorithm getActiveAlgorithm() {
                return "DILITHIUM".equalsIgnoreCase(algorithmName)
                                ? dilithiumSignature
                                : rsaSignature;
        }
}