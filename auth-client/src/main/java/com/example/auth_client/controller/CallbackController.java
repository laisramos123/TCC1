package com.example.auth_client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.example.auth_client.dto.TokenResponse;
import com.example.auth_client.service.TokenService;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;

@Controller
public class CallbackController {

    @Autowired
    private TokenService tokenService;

    /**
     * PASSO 4: Callback OAuth2
     * Banco redireciona aqui após usuário autorizar
     */
    @GetMapping("/callback")
    public RedirectView handleCallback(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String error,
            HttpSession session) {

        // Verifica se houve erro
        if (error != null) {
            return new RedirectView("/error?message=" + error);
        }

        try {
            // Recupera dados da sessão
            String storedState = (String) session.getAttribute("state");
            String codeVerifier = (String) session.getAttribute("code_verifier");
            String consentId = (String) session.getAttribute("consent_id");

            // Valida state (proteção CSRF)
            if (!state.equals(storedState)) {
                return new RedirectView("/error?message=Invalid state");
            }

            // PASSO 5: Troca código por tokens
            TokenResponse tokenResponse = tokenService.exchangeCodeForToken(
                    code,
                    codeVerifier);

            // Armazena tokens na sessão
            session.setAttribute("access_token", tokenResponse.getAccessToken());
            session.setAttribute("refresh_token", tokenResponse.getRefreshToken());
            session.setAttribute("token_expires_at",
                    Instant.now().plusSeconds(tokenResponse.getExpiresIn()));

            // Limpa dados temporários
            session.removeAttribute("code_verifier");
            session.removeAttribute("state");

            // Redireciona para página de contas
            return new RedirectView("/accounts");

        } catch (Exception e) {
            e.printStackTrace();
            return new RedirectView("/error?message=" + e.getMessage());
        }
    }
}
