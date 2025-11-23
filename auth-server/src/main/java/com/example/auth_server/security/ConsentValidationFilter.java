package com.example.auth_server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ConsentValidationFilter extends OncePerRequestFilter {

    private final ConsentAwareAuthorizationProvider consentProvider;

    public ConsentValidationFilter(ConsentAwareAuthorizationProvider consentProvider) {
        this.consentProvider = consentProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!request.getRequestURI().contains("/oauth2/authorize")) {
            filterChain.doFilter(request, response);
            return;
        }

        String scope = request.getParameter("scope");

        if (scope != null && !scope.isEmpty()) {
            try {

                consentProvider.validateConsentBeforeAuthorization(scope);

                logger.info("Consentimento validado com sucesso para scope: " + scope);

            } catch (Exception e) {
                logger.error("Erro ao validar consentimento: " + e.getMessage());

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(String.format(
                        "{\"error\":\"invalid_consent\",\"error_description\":\"%s\"}",
                        e.getMessage().replace("\"", "'")));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}