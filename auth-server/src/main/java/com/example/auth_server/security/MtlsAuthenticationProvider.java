package com.example.auth_server.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Provider de autenticação mTLS
 * Valida certificados X.509 do cliente
 */
@Component
public class MtlsAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (authentication instanceof PreAuthenticatedAuthenticationToken) {

            X509Certificate certificate = (X509Certificate) authentication.getCredentials();

            // Valida o certificado
            if (isValidCertificate(certificate)) {

                String clientId = extractClientId(certificate);

                // Cria autenticação com authorities
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

    /**
     * Valida certificado X.509
     */
    private boolean isValidCertificate(X509Certificate cert) {
        try {
            // Verifica validade temporal
            cert.checkValidity();

            // Verifica emissor (CA)
            String issuer = cert.getIssuerDN().getName();
            if (!issuer.contains("OpenFinance-CA") && !issuer.contains("ICP-Brasil")) {
                return false;
            }

            // TODO: Validar cadeia completa do certificado
            // TODO: Verificar CRL (Certificate Revocation List)
            // TODO: Validar OCSP (Online Certificate Status Protocol)

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrai Client ID do certificado
     */
    private String extractClientId(X509Certificate cert) {
        String dn = cert.getSubjectDN().getName();

        // Extrai CN (Common Name)
        String[] parts = dn.split(",");
        for (String part : parts) {
            if (part.trim().startsWith("CN=")) {
                return part.trim().substring(3);
            }
        }

        return "unknown-client";
    }
}