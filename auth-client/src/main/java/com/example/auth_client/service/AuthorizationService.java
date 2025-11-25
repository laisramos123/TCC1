package com.example.auth_client.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AuthorizationService {

    // ✅ CRÍTICO: Esta URL será usada para REDIRECIONAR O NAVEGADOR
    // Deve ser "localhost" porque o navegador não resolve nomes Docker!
    @Value("${tpp.bank.authorization-server}")
    private String authorizationServer;

    @Value("${tpp.client-id}")
    private String clientId;

    @Value("${tpp.redirect-uri}")
    private String redirectUri;

    /**
     * Constrói a URL de autorização OAuth2 para o navegador
     * 
     * IMPORTANTE: Esta URL será enviada ao navegador via redirect 302
     * Por isso DEVE usar "localhost" e NÃO "auth-server"
     */
    public String buildAuthorizationUrl(String consentId, String state, String codeVerifier, String nonce) {
        // Gerar code_challenge a partir do code_verifier
        String codeChallenge = generateCodeChallenge(codeVerifier);

        // Construir scope com consentId
        String scope = "openid consent:" + consentId + " accounts";

        // ✅ authorizationServer deve ser: http://localhost:8080
        // ❌ NUNCA deve ser: http://auth-server:8080
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

        // ✅ Se aparecer "localhost:8080" → CORRETO
        // ❌ Se aparecer "auth-server:8080" → ERRADO

        return authUrl;
    }

    /**
     * Gera o code_challenge a partir do code_verifier (PKCE)
     */
    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar code_challenge", e);
        }
    }

    /**
     * Gera um code_verifier aleatório (PKCE)
     */
    public String generateCodeVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Gera um state aleatório
     */
    public String generateState() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Gera um nonce aleatório
     */
    public String generateNonce() {
        return java.util.UUID.randomUUID().toString();
    }
}