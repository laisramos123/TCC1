package com.example.resource_server.config;

import com.example.resource_server.security.CertificateBoundAccessTokenValidator;
import com.example.resource_server.security.ConsentValidationFilter;
import com.example.resource_server.security.JwtAuthenticationConverter;
import com.example.resource_server.service.ConsentValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig {

        @Autowired
        private ConsentValidationService consentValidationService;

        @Autowired
        private CertificateBoundAccessTokenValidator certificateValidator;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/actuator/health", "/error").permitAll()
                                                .anyRequest().authenticated())

                                // Configuração como Resource Server com validação de certificado
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter())
                                                                .decoder(jwtDecoder()) // Decoder customizado com
                                                                                       // validação
                                                                                       // de certificado
                                                ))

                                // Adiciona filtro de validação de consentimento
                                .addFilterAfter(
                                                new ConsentValidationFilter(consentValidationService),
                                                UsernamePasswordAuthenticationFilter.class)

                                // Adiciona suporte a autenticação X.509
                                .x509(x509 -> x509
                                                .subjectPrincipalRegex("CN=(.*?)(?:,|$)"))

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

        /**
         * JWT Decoder com validação de Certificate Binding
         */
        @Bean
        public JwtDecoder jwtDecoder() {
                NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation("https://localhost:9000");

                // Adiciona validador de certificate binding
                jwtDecoder.setJwtValidator(certificateValidator);

                return jwtDecoder;
        }
}