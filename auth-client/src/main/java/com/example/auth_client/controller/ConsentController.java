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

    public RedirectView initiateConsent(
            @RequestParam String cpf,
            @RequestParam List<String> permissions,
            HttpSession session) {

        try {

            ConsentResponse consent = consentService.createConsent(cpf, permissions);
            String consentId = consent.getData().getConsentId();

            String codeVerifier = pkceGenerator.generateCodeVerifier();
            String state = UUID.randomUUID().toString();
            String nonce = UUID.randomUUID().toString();

            session.setAttribute("consent_id", consentId);
            session.setAttribute("code_verifier", codeVerifier);
            session.setAttribute("state", state);
            session.setAttribute("nonce", nonce);

            String authUrl = authorizationService.buildAuthorizationUrl(
                    consentId,
                    state,
                    codeVerifier,
                    nonce);

            return new RedirectView(authUrl);

        } catch (Exception e) {
            return new RedirectView("/error?message=" + e.getMessage());
        }
    }

    @GetMapping("/{consentId}")
    @ResponseBody
    public ConsentResponse getConsentStatus(@PathVariable String consentId) {
        return consentService.getConsent(consentId);
    }
}