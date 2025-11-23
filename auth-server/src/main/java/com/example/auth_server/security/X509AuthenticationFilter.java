package com.example.auth_server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.cert.X509Certificate;

@Component
public class X509AuthenticationFilter extends X509AuthenticationFilter {
    private static final Logger logger = LoggerFactory.getLogger(X509AuthenticationFilter.class);

    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("jakarta.servlet.request.X509Certificate");

        if (certs != null && certs.length > 0) {
            logger.debug("Certificado X.509 encontrado: {}", certs[0].getSubjectDN());
            return certs[0].getSubjectDN().toString();
        }

        logger.debug("Nenhum certificado X.509 encontrado");
        return null;
    }

    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("jakarta.servlet.request.X509Certificate");

        if (certs != null && certs.length > 0) {
            logger.debug("Processando autenticação X.509");
            super.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }
}
