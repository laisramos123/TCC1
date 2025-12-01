package com.example.auth_client.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

        String scopeEncoded = URLEncoder.encode(scope, StandardCharsets.UTF_8).replace("+", "%20");
        String clientIdEncoded = URLEncoder.encode(clientId, StandardCharsets.UTF_8).replace("+", "%20");
        String redirectUriEncoded = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8).replace("+", "%20");
        String stateEncoded = URLEncoder.encode(state, StandardCharsets.UTF_8).replace("+", "%20");
        String codeChallengeEncoded = URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8).replace("+", "%20");
        String nonceEncoded = URLEncoder.encode(nonce, StandardCharsets.UTF_8).replace("+", "%20");

        String authUrl = String.format(
                "%s/oauth2/authorize?response_type=%s&client_id=%s&redirect_uri=%s&scope=%s&state=%s&code_challenge=%s&code_challenge_method=%s&nonce=%s",
                authorizationServer,
                "code",
                clientIdEncoded,
                redirectUriEncoded,
                scopeEncoded,
                stateEncoded,
                codeChallengeEncoded,
                "S256",
                nonceEncoded);

        System.out.println("===========================================");
        System.out.println("  AUTHORIZATION URL GERADA:");
        System.out.println(authUrl);
        System.out.println("===========================================");
        System.out.println("  Scope original: " + scope);
        System.out.println("  Scope encodado: " + scopeEncoded);
        System.out.println("  ConsentId: " + consentId);
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