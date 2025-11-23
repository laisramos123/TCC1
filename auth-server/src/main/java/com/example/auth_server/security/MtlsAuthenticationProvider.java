package com.example.auth_server.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.util.List;

@Component
public class MtlsAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (authentication instanceof PreAuthenticatedAuthenticationToken) {

            X509Certificate certificate = (X509Certificate) authentication.getCredentials();

            if (isValidCertificate(certificate)) {

                String clientId = extractClientId(certificate);

                return new PreAuthenticatedAuthenticationToken(
                        clientId,
                        certificate,
                        List.of(new SimpleGrantedAuthority("ROLE_TPP")));
            }
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private boolean isValidCertificate(X509Certificate cert) {
        try {

            cert.checkValidity();

            String issuer = cert.getIssuerDN().getName();
            if (!issuer.contains("OpenFinance-CA") && !issuer.contains("ICP-Brasil")) {
                return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private String extractClientId(X509Certificate cert) {
        String dn = cert.getSubjectDN().getName();

        String[] parts = dn.split(",");
        for (String part : parts) {
            if (part.trim().startsWith("CN=")) {
                return part.trim().substring(3);
            }
        }

        return "unknown-client";
    }
}