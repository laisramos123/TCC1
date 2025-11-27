package com.example.auth_client.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AuthorizationService {

    @Value("${tpp.bank.authorization-server}")
    private String authorizationServer;

    @Value("${tpp.client-id}")
    private String clientId;

    @Value("${tpp.redirect-uri}")
    private String redirectUri;

    public String buildAuthorizationUrl(String consentId, String state, String codeVerifier, String nonce) {

        String codeChallenge = generateCodeChallenge(codeVerifier);

        String scope = "openid consent:" + consentId + " accounts";

        String authUrl = UriComponentsBuilder
                .fromHttpUrl(authorizationServer + "/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scope)
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .queryParam("nonce", nonce)
                .build()
                .toUriString();

        // Log para verificação (facilita debugging)
        System.out.println("===========================================");
        System.out.println("🔗 AUTHORIZATION URL GERADA:");
        System.out.println(authUrl);
        System.out.println("===========================================");

        return authUrl;
    }

    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar code_challenge", e);
        }
    }

    public String generateCodeVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String generateState() {
        return java.util.UUID.randomUUID().toString();
    }

    public String generateNonce() {
        return java.util.UUID.randomUUID().toString();
    }
}