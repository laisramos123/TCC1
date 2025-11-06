package com.example.auth_client.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.auth_client.dto.ConsentResponse;

@Service
public class AuthorizationService {

    @Value("${tpp.client-id}")
    private String clientId;

    @Value("${tpp.redirect-uri}")
    private String redirectUri;

    @Value("${tpp.bank.authorization-server}")
    private String authorizationServer;

    private final PkceGenerator pkceGenerator;

    public AuthorizationService(PkceGenerator pkceGenerator) {
        this.pkceGenerator = pkceGenerator;
    }

    /**
     * PASSO 2: Constrói URL de autorização
     */
    public String buildAuthorizationUrl(ConsentResponse consent,
            String codeVerifier,
            String state) {

        String codeChallenge = pkceGenerator.generateCodeChallenge(codeVerifier);

        String consentId = consent.getData().getConsentId();
        String scope = "openid consent:" + consentId + " accounts";

        return UriComponentsBuilder
                .fromHttpUrl(authorizationServer + "/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scope)
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .queryParam("nonce", UUID.randomUUID().toString())
                .build()
                .toUriString();
    }
}