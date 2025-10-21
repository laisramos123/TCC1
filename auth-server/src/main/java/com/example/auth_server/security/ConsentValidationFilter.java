package com.example.auth_server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * FASE 2 - PASSO 1: Filtro que valida consentimento
 * Intercepta /oauth2/authorize ANTES do processamento OAuth2
 */
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

        // Só processa endpoint de autorização
        if (!request.getRequestURI().contains("/oauth2/authorize")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrai scope do request
        String scope = request.getParameter("scope");

        if (scope != null && !scope.isEmpty()) {
            try {
                // VALIDA CONSENTIMENTO via Consent API
                consentProvider.validateConsentBeforeAuthorization(scope);

                logger.info("Consentimento validado com sucesso para scope: " + scope);

            } catch (Exception e) {
                logger.error("Erro ao validar consentimento: " + e.getMessage());

                // Retorna erro OAuth2
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(String.format(
                        "{\"error\":\"invalid_consent\",\"error_description\":\"%s\"}",
                        e.getMessage().replace("\"", "'")));
                return;
            }
        }

        // Continua o fluxo
        filterChain.doFilter(request, response);
    }
}