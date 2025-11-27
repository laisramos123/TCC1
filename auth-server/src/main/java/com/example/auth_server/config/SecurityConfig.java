package com.example.auth_server.config;

import com.example.auth_server.security.ConsentAwareAuthorizationProvider;
import com.example.auth_server.security.ConsentValidationFilter;
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
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final CustomUserDetailsService customUserDetailsService;
        private final ConsentAwareAuthorizationProvider consentProvider;

        public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                        ConsentAwareAuthorizationProvider consentProvider) {
                this.customUserDetailsService = customUserDetailsService;
                this.consentProvider = consentProvider;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        @Order(1)
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
                OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

                http.addFilterBefore(
                                new ConsentValidationFilter(consentProvider),
                                AuthorizationFilter.class);
                http.exceptionHandling((exceptions) -> exceptions
                                .defaultAuthenticationEntryPointFor(
                                                new LoginUrlAuthenticationEntryPoint("/login"),
                                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

                return http.build();
        }

        @Bean
        @Order(2)
        public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
                http

                                .userDetailsService(customUserDetailsService)

                                .authorizeHttpRequests((authorize) -> authorize

                                                .requestMatchers(
                                                                "/login",
                                                                "/error",
                                                                "/actuator/health",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**")
                                                .permitAll()

                                                .requestMatchers("/open-banking/**").permitAll()

                                                .requestMatchers("/api/v1/dilithium/**").permitAll()
                                                .requestMatchers("/api/v1/benchmark/**").permitAll()

                                                .anyRequest().authenticated())

                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/", true)
                                                .failureUrl("/login?error")
                                                .permitAll())

                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll())

                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers(
                                                                "/open-banking/**",
                                                                "/api/v1/dilithium/**",
                                                                "/api/v1/benchmark/**",
                                                                "/oauth2/token"));

                return http.build();
        }
}