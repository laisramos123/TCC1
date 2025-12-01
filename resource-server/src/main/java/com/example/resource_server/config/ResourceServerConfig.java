package com.example.resource_server.config;

import com.example.resource_server.jwt.CustomJwtDecoder;
import com.example.resource_server.security.ConsentValidationFilter;
import com.example.resource_server.security.JwtAuthenticationConverter;
import com.example.resource_server.service.ConsentValidationService;
import com.example.resource_server.service.PublicKeyService;
import com.example.resource_server.signature.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.PublicKey;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableScheduling
public class ResourceServerConfig {

        @Value("${jwt.signature.algorithm:DILITHIUM}")
        private String algorithmName;

        private final ConsentValidationService consentValidationService;
        private final SignatureAlgorithm rsaSignature;
        private final SignatureAlgorithm dilithiumSignature;
        private final PublicKeyService publicKeyService;

        public ResourceServerConfig(
                        ConsentValidationService consentValidationService,
                        @Qualifier("rsaSignature") SignatureAlgorithm rsaSignature,
                        @Qualifier("dilithiumSignature") SignatureAlgorithm dilithiumSignature,
                        @Lazy PublicKeyService publicKeyService) {

                this.consentValidationService = consentValidationService;
                this.rsaSignature = rsaSignature;
                this.dilithiumSignature = dilithiumSignature;
                this.publicKeyService = publicKeyService;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/actuator/health", "/actuator/prometheus", "/error")
                                                .permitAll()
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
                log.info("  Configurando JwtDecoder para algoritmo: {}", algorithmName);

                try {
                        SignatureAlgorithm algorithm = getActiveAlgorithm();

                        PublicKey publicKey = publicKeyService.getPublicKey(algorithmName);

                        if (publicKey != null) {
                                log.info("  Usando chave pública do auth-server para {} ({} bytes)",
                                                algorithmName, publicKey.getEncoded().length);
                                return new CustomJwtDecoder(algorithm, publicKey);
                        } else {
                                log.warn("  Chave do auth-server não disponível para {}", algorithmName);
                                log.warn("  Usando decoder que buscará a chave dinamicamente");

                                return new DynamicPublicKeyJwtDecoder(algorithm, publicKeyService, algorithmName);
                        }

                } catch (Exception e) {
                        log.error("  Falha ao criar JWT decoder: {}", e.getMessage());
                        throw new RuntimeException("Failed to create JWT decoder", e);
                }
        }

        private SignatureAlgorithm getActiveAlgorithm() {
                return "DILITHIUM".equalsIgnoreCase(algorithmName)
                                ? dilithiumSignature
                                : rsaSignature;
        }

        private static class DynamicPublicKeyJwtDecoder implements JwtDecoder {
                private final SignatureAlgorithm algorithm;
                private final PublicKeyService publicKeyService;
                private final String algorithmName;
                private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
                                .getLogger(DynamicPublicKeyJwtDecoder.class);

                public DynamicPublicKeyJwtDecoder(SignatureAlgorithm algorithm, PublicKeyService publicKeyService,
                                String algorithmName) {
                        this.algorithm = algorithm;
                        this.publicKeyService = publicKeyService;
                        this.algorithmName = algorithmName;
                }

                @Override
                public org.springframework.security.oauth2.jwt.Jwt decode(String token)
                                throws org.springframework.security.oauth2.jwt.JwtException {
                        PublicKey publicKey = publicKeyService.getPublicKey(algorithmName);

                        if (publicKey == null) {
                                log.error("  Chave pública não disponível para {}", algorithmName);
                                throw new org.springframework.security.oauth2.jwt.JwtException(
                                                "Public key not available for " + algorithmName);
                        }

                        CustomJwtDecoder decoder = new CustomJwtDecoder(algorithm, publicKey);
                        return decoder.decode(token);
                }
        }
}