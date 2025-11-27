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

        if (scope != null && scope.contains("consent:")) {
            try {

                String consentId = extractConsentId(scope);

                consentProvider.validateConsentBeforeAuthorization(scope);

                logger.info(" Consent validado: {}");

            } catch (Exception e) {
                logger.error(" Erro ao validar consent: {}");

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

    private String extractConsentId(String scope) {
        for (String s : scope.split(" ")) {
            if (s.startsWith("consent:")) {
                return s.substring(8);
            }
        }
        throw new IllegalArgumentException("consent_id não encontrado no scope");
    }
}