package com.example.auth_client.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.auth_client.dto.TokenResponse;

@Service
public class TokenService {

    @Value("${tpp.client-id}")
    private String clientId;

    @Value("${tpp.client-secret}")
    private String clientSecret;

    @Value("${tpp.redirect-uri}")
    private String redirectUri;

    @Value("${tpp.bank.authorization-server}")
    private String authorizationServer;

    private final RestTemplate restTemplate;

    public TokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * PASSO 3: Troca authorization code por tokens
     */
    public TokenResponse exchangeCodeForToken(String code, String codeVerifier) {

        String tokenEndpoint = authorizationServer + "/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                tokenEndpoint,
                request,
                TokenResponse.class);

        return response.getBody();
    }

    /**
     * Renova access token usando refresh token
     */
    public TokenResponse refreshToken(String refreshToken) {

        String tokenEndpoint = authorizationServer + "/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                tokenEndpoint,
                request,
                TokenResponse.class);

        return response.getBody();
    }
}