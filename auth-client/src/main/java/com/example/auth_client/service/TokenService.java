package com.example.auth_client.service;

import com.example.auth_client.dto.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${tpp.client-id}")
    private String clientId;

    @Value("${tpp.client-secret}")
    private String clientSecret;

    @Value("${tpp.redirect-uri}")
    private String redirectUri;

    @Value("${tpp.bank.authorization-server-internal}")
    private String authorizationServerInternal;

    private final RestTemplate restTemplate;

    public TokenService(@Qualifier("mtlsRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TokenResponse exchangeCodeForToken(String code, String codeVerifier) {
        logger.info("========================================");
        logger.info("  TROCA DE CÓDIGO POR TOKEN");
        logger.info("========================================");

        String tokenEndpoint = authorizationServerInternal + "/oauth2/token";

        logger.info("  Token Endpoint: {}", tokenEndpoint);
        logger.info("  Client ID: {}", clientId);
        logger.info("  Code: {}", code.substring(0, Math.min(code.length(), 20)) + "...");
        logger.info("  Code Verifier: {}", codeVerifier.substring(0, Math.min(codeVerifier.length(), 20)) + "...");

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String auth = clientId + ":" + clientSecret;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.set("Authorization", authHeader);

            logger.info("  Authorization Header: Basic [REDACTED]");

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", redirectUri);
            body.add("code_verifier", codeVerifier);

            logger.info("  Request Body:");
            logger.info("   grant_type: authorization_code");
            logger.info("   code: {}...", code.substring(0, Math.min(code.length(), 20)));
            logger.info("   redirect_uri: {}", redirectUri);
            logger.info("   code_verifier: {}...", codeVerifier.substring(0, Math.min(codeVerifier.length(), 20)));

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            logger.info("  Enviando requisição para: {}", tokenEndpoint);

            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    tokenEndpoint,
                    request,
                    TokenResponse.class);

            logger.info("========================================");
            logger.info("  TOKEN OBTIDO COM SUCESSO!");
            logger.info("========================================");
            logger.info("  Status Code: {}", response.getStatusCode());

            if (response.getBody() != null) {
                logger.info("  Access Token: {}...",
                        response.getBody().getAccessToken().substring(0,
                                Math.min(30, response.getBody().getAccessToken().length())));
                logger.info("  Expires In: {} segundos", response.getBody().getExpiresIn());
                logger.info("  Refresh Token: {}",
                        response.getBody().getRefreshToken() != null ? "Presente" : "Ausente");
            }
            logger.info("========================================");

            return response.getBody();

        } catch (Exception e) {
            logger.error("========================================");
            logger.error("  ERRO NA TROCA DE CÓDIGO POR TOKEN");
            logger.error("========================================");
            logger.error("  Erro: {}", e.getMessage());
            logger.error("  Endpoint: {}", tokenEndpoint);
            logger.error("  Client ID: {}", clientId);
            if (e.getCause() != null) {
                logger.error("🔍 Causa raiz: {}", e.getCause().getMessage());
            }

            logger.error("========================================");
            throw new RuntimeException("Falha ao trocar código por token: " + e.getMessage(), e);
        }
    }

    public TokenResponse refreshToken(String refreshToken) {
        logger.info("  Renovando access token com refresh token...");

        String tokenEndpoint = authorizationServerInternal + "/oauth2/token";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String auth = clientId + ":" + clientSecret;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.set("Authorization", authHeader);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    tokenEndpoint,
                    request,
                    TokenResponse.class);

            logger.info("  Token renovado com sucesso!");
            return response.getBody();

        } catch (Exception e) {
            logger.error("  Erro ao renovar token: {}", e.getMessage());
            throw new RuntimeException("Falha ao renovar token: " + e.getMessage(), e);
        }
    }
}