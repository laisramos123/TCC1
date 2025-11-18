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

@Component
public class CertificateBoundAccessTokenValidator implements OAuth2TokenValidator<Jwt> {

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        try {

            X509Certificate clientCert = getClientCertificate();

            if (clientCert == null) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Client certificate not found", null));
            }

            String jwtThumbprint = extractThumbprintFromJwt(jwt);

            if (jwtThumbprint == null) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Certificate thumbprint not found in token", null));
            }

            String certThumbprint = calculateThumbprint(clientCert);

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

    private String extractThumbprintFromJwt(Jwt jwt) {
        Object cnf = jwt.getClaim("cnf");
        if (cnf instanceof java.util.Map) {
            java.util.Map<?, ?> cnfMap = (java.util.Map<?, ?>) cnf;
            return (String) cnfMap.get("x5t#S256");
        }
        return null;
    }

    private String calculateThumbprint(X509Certificate cert) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}