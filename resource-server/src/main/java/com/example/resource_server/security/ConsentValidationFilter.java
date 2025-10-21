package com.example.resource_server.security;

import com.example.resource_server.service.ConsentValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * FASE 3 - PASSO 1: Filtro que valida consentimento em TODA request
 */
public class ConsentValidationFilter extends OncePerRequestFilter {

    private final ConsentValidationService consentValidationService;

    public ConsentValidationFilter(ConsentValidationService consentValidationService) {
        this.consentValidationService = consentValidationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extrai JWT do contexto de segurança
            JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext()
                    .getAuthentication();

            if (authentication != null) {
                Jwt jwt = authentication.getToken();

                // Extrai consent ID do JWT
                String consentId = jwt.getClaimAsString("consent_id");

                if (consentId == null) {
                    // Tenta extrair do scope
                    String scope = jwt.getClaimAsString("scope");
                    consentId = extractConsentIdFromScope(scope);
                }

                if (consentId == null) {
                    throw new RuntimeException("Consent ID não encontrado no token");
                }

                // Determina permissão necessária baseado no endpoint
                String requiredPermission = getRequiredPermission(request.getRequestURI());

                // VALIDA CONSENTIMENTO via Consent API
                consentValidationService.validateConsentForResourceAccess(
                        consentId,
                        requiredPermission);

                logger.debug("Consentimento validado: " + consentId);
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Erro ao validar consentimento: " + e.getMessage());

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(String.format(
                    "{\"error\":\"invalid_consent\",\"error_description\":\"%s\"}",
                    e.getMessage().replace("\"", "'")));
        }
    }

    private String extractConsentIdFromScope(String scope) {
        if (scope == null)
            return null;

        for (String s : scope.split(" ")) {
            if (s.startsWith("consent:")) {
                return s.substring(8);
            }
        }
        return null;
    }

    private String getRequiredPermission(String uri) {
        if (uri.contains("/accounts") && !uri.contains("/balances") && !uri.contains("/transactions")) {
            return "ACCOUNTS_READ";
        } else if (uri.contains("/balances")) {
            return "ACCOUNTS_BALANCES_READ";
        } else if (uri.contains("/transactions")) {
            return "ACCOUNTS_TRANSACTIONS_READ";
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/actuator/health") || path.equals("/error");
    }
}