package com.example.auth_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança geral (login, etc)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        /**
         * Security filter chain padrão (login form)
         */
        @Bean
        @Order(2)
        public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
                        throws Exception {

                http
                                .authorizeHttpRequests((authorize) -> authorize
                                                // Endpoints públicos
                                                .requestMatchers(
                                                                "/",
                                                                "/login",
                                                                "/error",
                                                                "/oauth2/jwks",
                                                                "/open-banking/consents/v2/consents/**")
                                                .permitAll()

                                                // Tudo mais precisa autenticação
                                                .anyRequest().authenticated())

                                // Form login
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .permitAll())

                                // Logout
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/")
                                                .permitAll());

                return http.build();
        }

        /**
         * Usuários do banco (clientes finais)
         * Em produção: buscar do banco de dados
         */
        @Bean
        public UserDetailsService userDetailsService() {

                UserDetails user = User.builder()
                                .username("joao.silva")
                                .password(passwordEncoder().encode("senha123"))
                                .roles("USER")
                                .build();

                UserDetails admin = User.builder()
                                .username("admin")
                                .password(passwordEncoder().encode("admin"))
                                .roles("USER", "ADMIN")
                                .build();

                return new InMemoryUserDetailsManager(user, admin);
        }

        /**
         * Password encoder
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}