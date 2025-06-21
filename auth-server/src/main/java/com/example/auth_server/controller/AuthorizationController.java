package com.example.auth_server.controller;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;

import com.example.auth_server.model.Consent;
import com.example.auth_server.service.ConsentService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthorizationController {

    private final RegisteredClientRepository clientRepository;
    private final ConsentService consentService;

    public AuthorizationController(RegisteredClientRepository clientRepository, ConsentService consentService) {
        this.clientRepository = clientRepository;
        this.consentService = consentService;
    }

    @GetMapping("/oauth2/consent")
    public String consentPage(
            Principal principal,
            @RequestParam(OAuth2ParameterNames.CLIENT_ID) String client_id,
            @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
            @RequestParam(OAuth2ParameterNames.STATE) String state,
            Model model,
            HttpServletRequest request) {

        // Validar cliente
        RegisteredClient client = clientRepository.findByClientId(client_id);
        if (client == null) {
            throw new OAuth2AuthenticationException("Cliente inválido");
        }

        // Criar consentimento pendente
        Set<String> permissions = Arrays.stream(scope.split(" "))
                .collect(Collectors.toSet());

        Consent consent = consentService.createConsent(
                principal.getName(),
                client_id,
                permissions);

        // Preparar dados para a tela
        model.addAttribute("client_id", client_id);
        model.addAttribute("clientName", client.getClientName());
        model.addAttribute("scopes", permissions);
        model.addAttribute("consentId", consent.getConsentId());
        model.addAttribute("expiresAt", consent.getExpiresAt());

        // Dados obrigatórios Open Finance
        // model.addAttribute("dataRecipient", getDataRecipientInfo(client_id));
        model.addAttribute("dataSharingPurpose", "Compartilhamento de dados financeiros");
        model.addAttribute("consentExpirationDate",
                consent.getExpiresAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        return "open-finance-consent"; // Template específico Open Finance
    }

}