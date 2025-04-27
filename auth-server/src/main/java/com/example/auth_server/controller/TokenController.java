package com.example.auth_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_server.model.TokenResponse;
import com.example.auth_server.service.TokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class TokenController {
    @Autowired
    private TokenService tokenService;

    @PostMapping("/oauth/token")
    public ResponseEntity<TokenResponse> exchangeToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code") String code) {
        // TODO: Validar cliente (via private_key_jwt)
        // Verificar se o código de autorização é válido e não expirou
        // Verificar se o código de autorização pertence ao cliente
        // Verificar se o código de autorização foi usado
        // Verificar se o redirect_uri é o mesmo que o usado na autorização

        if ("authorization_code".equals(grantType)) {
            // TODO: Gerar o token de acesso e o refresh token
            TokenResponse tokenResponse = tokenService.generateToken(clientId, redirectUri, code);
            return ResponseEntity.ok(tokenResponse);
        } else {
            return ResponseEntity.badRequest().body(new TokenResponse("invalid_grant", "Invalid grant type"));
        }
    }

}
