package com.example.auth_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.auth_server.dto.ConsentResponse;
import com.example.auth_server.security.ConsentAwareAuthorizationProvider;
import com.example.auth_server.service.ConsentService;

@Controller
public class ConsentController {

    @Autowired
    private ConsentService consentService;

    @Autowired
    private ConsentAwareAuthorizationProvider consentProvider;

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

        String consentId = extractConsentId(scope);

        if (consentId == null) {
            return "redirect:/error?message=Consent ID não encontrado";
        }

        try {

            ConsentResponse consent = consentService.getConsent(consentId);

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

    @PostMapping("/oauth2/consent/approve")
    public String approveConsent(
            @RequestParam String state,
            @RequestParam String scope) {

        consentProvider.updateConsentAfterAuthorization(scope);

        return "redirect:/oauth2/authorize?state=" + state;
    }

    @PostMapping("/oauth2/consent/deny")
    public String denyConsent(
            @RequestParam String state,
            @RequestParam String scope) {

        consentProvider.markConsentAsRejected(scope, "CUSTOMER_MANUALLY_REJECTED");

        return "redirect:/oauth2/authorize?error=access_denied&state=" + state;
    }

    private String extractConsentId(String scope) {
        for (String s : scope.split(" ")) {
            if (s.startsWith("consent:")) {
                return s.substring(8);
            }
        }
        return null;
    }
}