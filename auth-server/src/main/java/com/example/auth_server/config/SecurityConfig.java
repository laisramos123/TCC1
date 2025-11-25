package com.example.auth_server.config;

import com.example.auth_server.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * ✅ OBRIGATÓRIO: Bean do PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security Filter Chain para OAuth2 Authorization Server
     * Ordem 1 = processa PRIMEIRO (endpoints OAuth2)
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.exceptionHandling((exceptions) -> exceptions
                .defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

        return http.build();
    }

    /**
     * Security Filter Chain padrão
     * Ordem 2 = processa DEPOIS (demais endpoints)
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // ✅ Configurar o UserDetailsService customizado
                .userDetailsService(customUserDetailsService)

                .authorizeHttpRequests((authorize) -> authorize
                        // Endpoints públicos - sem autenticação
                        .requestMatchers(
                                "/login",
                                "/error",
                                "/actuator/health",
                                "/css/**",
                                "/js/**",
                                "/images/**")
                        .permitAll()

                        // ✅ CRÍTICO: API de consentimentos do Open Banking DEVE ser pública
                        // Requisições vindas do auth-client não têm autenticação
                        .requestMatchers("/open-banking/**").permitAll()

                        // Dilithium API (pode ser pública para demonstração)
                        .requestMatchers("/api/v1/dilithium/**").permitAll()

                        // Todos os outros endpoints requerem autenticação
                        .anyRequest().authenticated())

                // Configuração do formulário de login
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll())

                // Logout
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())

                // ✅ CRÍTICO: Desabilitar CSRF para APIs REST do Open Banking
                // APIs REST entre microserviços não usam CSRF (apenas para formulários HTML)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/open-banking/**",
                                "/api/v1/dilithium/**",
                                "/oauth2/token" // Endpoint OAuth2 de token
                        ));

        return http.build();
    }
}