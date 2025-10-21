package com.example.auth_client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import com.example.auth_client.dto.ConsentResponse;
import com.example.auth_client.service.AuthorizationService;
import com.example.auth_client.service.ConsentService;
import com.example.auth_client.service.PkceGenerator;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/consent")
public class ConsentController {

    @Autowired
    private ConsentService consentService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private PkceGenerator pkceGenerator;

    /**
     * FLUXO COMPLETO:
     * 1. Cria consentimento
     * 2. Redireciona para autorização OAuth2
     */
    @PostMapping("/initiate")
    public RedirectView initiateConsent(
            @RequestParam String cpf,
            @RequestParam List<String> permissions,
            HttpSession session) {

        try {
            // PASSO 1: Criar consentimento no banco
            ConsentResponse consent = consentService.createConsent(cpf, permissions);

            // Gera PKCE
            String codeVerifier = pkceGenerator.generateCodeVerifier();
            String state = UUID.randomUUID().toString();

            // Armazena na sessão
            session.setAttribute("consent_id", consent.getData().getConsentId());
            session.setAttribute("code_verifier", codeVerifier);
            session.setAttribute("state", state);

            // PASSO 2: Constrói URL de autorização
            String authUrl = authorizationService.buildAuthorizationUrl(
                    consent,
                    codeVerifier,
                    state);

            // PASSO 3: Redireciona usuário para o banco autorizar
            return new RedirectView(authUrl);

        } catch (Exception e) {
            return new RedirectView("/error?message=" + e.getMessage());
        }
    }

    /**
     * Consulta status de um consentimento
     */
    @GetMapping("/{consentId}")
    @ResponseBody
    public ConsentResponse getConsentStatus(@PathVariable String consentId) {
        return consentService.getConsent(consentId);
    }
}