package com.example.resource_server.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * Validador de Certificate-Bound Access Tokens (RFC 8705)
 * Garante que o token JWT está vinculado ao certificado do cliente
 */
@Component
public class CertificateBoundAccessTokenValidator implements OAuth2TokenValidator<Jwt> {

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        try {
            // Obtém certificado da requisição atual
            X509Certificate clientCert = getClientCertificate();

            if (clientCert == null) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Client certificate not found", null));
            }

            // Extrai thumbprint do JWT (cnf claim)
            String jwtThumbprint = extractThumbprintFromJwt(jwt);

            if (jwtThumbprint == null) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Certificate thumbprint not found in token", null));
            }

            // Calcula thumbprint do certificado atual
            String certThumbprint = calculateThumbprint(clientCert);

            // Compara thumbprints
            if (!jwtThumbprint.equals(certThumbprint)) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Certificate thumbprint mismatch", null));
            }

            return OAuth2TokenValidatorResult.success();

        } catch (Exception e) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Error validating certificate binding", null));
        }
    }

    /**
     * Obtém certificado X.509 da requisição HTTP
     */
    private X509Certificate getClientCertificate() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");

            if (certs != null && certs.length > 0) {
                return certs[0];
            }
        }

        return null;
    }

    /**
     * Extrai thumbprint do JWT (claim "cnf")
     * Conforme RFC 8705
     */
    private String extractThumbprintFromJwt(Jwt jwt) {
        Object cnf = jwt.getClaim("cnf");
        if (cnf instanceof java.util.Map) {
            java.util.Map<?, ?> cnfMap = (java.util.Map<?, ?>) cnf;
            return (String) cnfMap.get("x5t#S256");
        }
        return null;
    }

    /**
     * Calcula SHA-256 thumbprint do certificado
     * Conforme RFC 8705
     */
    private String calculateThumbprint(X509Certificate cert) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}