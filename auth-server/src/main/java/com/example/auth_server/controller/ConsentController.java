package com.example.auth_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.auth_server.dto.ConsentResponse;
import com.example.auth_server.security.ConsentAwareAuthorizationProvider;
import com.example.auth_server.service.ConsentService;

/**
 * FASE 2 - PASSO 2: Controller da tela de consentimento
 */
@Controller
public class ConsentController {

    @Autowired
    private ConsentService consentService;

    @Autowired
    private ConsentAwareAuthorizationProvider consentProvider;

    /**
     * Exibe página de consentimento
     * Chamada automaticamente pelo Spring Authorization Server
     */
    @GetMapping("/oauth2/consent")
    public String consentPage(
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "client_id", required = false) String clientId,
            @RequestParam(name = "scope", required = false) String scope,
            Model model,
            Authentication authentication) {

        if (scope == null) {
            return "redirect:/error?message=Scope não fornecido";
        }

        // Extrai consent ID do scope
        String consentId = extractConsentId(scope);

        if (consentId == null) {
            return "redirect:/error?message=Consent ID não encontrado";
        }

        try {
            // Busca detalhes do consentimento via Consent API
            ConsentResponse consent = consentService.getConsent(consentId);

            // Adiciona dados ao model
            model.addAttribute("consentId", consentId);
            model.addAttribute("clientId", clientId);
            model.addAttribute("scope", scope);
            model.addAttribute("state", state);
            model.addAttribute("username", authentication != null ? authentication.getName() : "Usuário");
            model.addAttribute("permissions", consent.getData().getPermissions());
            model.addAttribute("expirationDate", consent.getData().getExpirationDateTime());

            return "consent";

        } catch (Exception e) {
            return "redirect:/error?message=" + e.getMessage();
        }
    }

    /**
     * Usuário APROVA o consentimento
     */
    @PostMapping("/oauth2/consent/approve")
    public String approveConsent(
            @RequestParam String state,
            @RequestParam String scope) {

        // Atualiza status do consentimento para AUTHORISED
        consentProvider.updateConsentAfterAuthorization(scope);

        // Redireciona de volta para o fluxo OAuth2
        return "redirect:/oauth2/authorize?state=" + state;
    }

    /**
     * Usuário NEGA o consentimento
     */
    @PostMapping("/oauth2/consent/deny")
    public String denyConsent(
            @RequestParam String state,
            @RequestParam String scope) {

        // Marca consentimento como REJECTED
        consentProvider.markConsentAsRejected(scope, "CUSTOMER_MANUALLY_REJECTED");

        // Redireciona com erro
        return "redirect:/oauth2/authorize?error=access_denied&state=" + state;
    }

    /**
     * Extrai consent ID do scope
     */
    private String extractConsentId(String scope) {
        for (String s : scope.split(" ")) {
            if (s.startsWith("consent:")) {
                return s.substring(8);
            }
        }
        return null;
    }
}