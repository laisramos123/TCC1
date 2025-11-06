package com.example.auth_server.config;

import com.example.auth_server.security.MtlsAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private MtlsAuthenticationProvider mtlsAuthenticationProvider;

        @Autowired
        private X509AuthenticationFilter x509AuthenticationFilter;

        /**
         * Security filter chain com suporte a mTLS
         */
        @Bean
        @Order(2)
        public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(
                                                                "/",
                                                                "/login",
                                                                "/error",
                                                                "/oauth2/jwks",
                                                                "/open-banking/consents/v2/consents/**")
                                                .permitAll()
                                                .anyRequest().authenticated())

                                // Adiciona filtro X.509 para mTLS
                                .addFilter(x509AuthenticationFilter)

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
         * Configura Authentication Manager com mTLS provider
         */
        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
                AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

                authBuilder.authenticationProvider(mtlsAuthenticationProvider);

                return authBuilder.build();
        }

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

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}