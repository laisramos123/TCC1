package com.example.auth_server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.cert.X509Certificate;

@Component
public class X509AuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(X509AuthenticationFilter.class);

    private AuthenticationManager authenticationManager;

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("jakarta.servlet.request.X509Certificate");

        if (certs != null && certs.length > 0) {
            logger.debug("Certificado X.509 encontrado: {}", certs[0].getSubjectDN());

            try {
                String principal = certs[0].getSubjectDN().toString();

                if (authenticationManager != null) {
                    PreAuthenticatedAuthenticationToken authRequest = new PreAuthenticatedAuthenticationToken(principal,
                            certs);

                    Authentication authResult = authenticationManager.authenticate(authRequest);

                    if (authResult != null && authResult.isAuthenticated()) {
                        SecurityContextHolder.getContext().setAuthentication(authResult);
                        logger.debug("Autenticação X.509 bem-sucedida para: {}", principal);
                    }
                }
            } catch (Exception e) {
                logger.error("Erro na autenticação X.509", e);
            }
        } else {
            logger.trace("Nenhum certificado X.509 encontrado no request");
        }

        filterChain.doFilter(request, response);
    }

    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("jakarta.servlet.request.X509Certificate");

        if (certs != null && certs.length > 0) {
            return certs[0].getSubjectDN().toString();
        }

        return null;
    }

    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("jakarta.servlet.request.X509Certificate");
        return certs != null && certs.length > 0 ? certs : "N/A";
    }
}