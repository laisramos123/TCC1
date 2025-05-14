package com.example.auth_server.controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.auth_server.service.ConsentService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ConsentController {

    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2AuthorizationConsentService authorizationConsentService;
    private final ConsentService consentService; // Serviço customizado para gerenciar consentimentos

    @Autowired
    public ConsentController(RegisteredClientRepository registeredClientRepository,
            OAuth2AuthorizationService authorizationService,
            OAuth2AuthorizationConsentService authorizationConsentService,
            ConsentService consentService) {
        this.registeredClientRepository = registeredClientRepository;
        this.authorizationService = authorizationService;
        this.authorizationConsentService = authorizationConsentService;
        this.consentService = consentService;
    }

    @GetMapping("/oauth2/consent")
    public String consentPage(
            @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
            @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
            @RequestParam(OAuth2ParameterNames.STATE) String state,
            @RequestParam(value = OAuth2ParameterNames.USER_CODE, required = false) String userCode,
            Principal principal,
            Model model) {

        // Busca informações do cliente
        RegisteredClient client = this.registeredClientRepository.findByClientId(clientId);
        if (client == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        // Busca consentimento existente
        OAuth2AuthorizationConsent consent = this.authorizationConsentService
                .findById(client.getId(), principal.getName());

        // Prepara os escopos para exibição
        Set<String> scopesToApprove = new HashSet<>();
        Set<String> previouslyApprovedScopes = new HashSet<>();

        for (String requestedScope : scope.split(" ")) {
            if (consent != null && consent.getScopes().contains(requestedScope)) {
                previouslyApprovedScopes.add(requestedScope);
            } else {
                scopesToApprove.add(requestedScope);
            }
        }

        // Busca descrições detalhadas dos escopos
        Map<String, String> scopeDescriptions = this.consentService.getScopeDescriptions(scopesToApprove);

        // Correção aqui - usando os métodos addAttribute corretos
        model.addAttribute("clientId", clientId);
        model.addAttribute("clientName", client.getClientName() != null ? client.getClientName() : clientId);
        model.addAttribute("state", state);
        model.addAttribute("scopes", scopesToApprove);
        model.addAttribute("scopeDescriptions", scopeDescriptions);
        model.addAttribute("previouslyApprovedScopes", previouslyApprovedScopes);

        if (userCode != null) {
            model.addAttribute("userCode", userCode);
        }

        return "consent";
    }

    @PostMapping("/oauth2/consent")
    public String consent(
            @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
            @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
            @RequestParam(OAuth2ParameterNames.STATE) String state,
            @RequestParam(value = OAuth2ParameterNames.USER_CODE, required = false) String userCode,
            @RequestParam(value = "consent_action", defaultValue = "approve") String consentAction,
            Principal principal) {

        RegisteredClient client = this.registeredClientRepository.findByClientId(clientId);
        if (client == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        // Processa a ação de consentimento
        OAuth2AuthorizationConsent.Builder consentBuilder = OAuth2AuthorizationConsent
                .withId(client.getId(), principal.getName());

        if ("approve".equals(consentAction)) {
            // Registra os escopos aprovados
            for (String requestedScope : scope.split(" ")) {
                consentBuilder.scope(requestedScope);
            }

            // Registra o consentimento no sistema
            String consentId = this.consentService.registerConsent(
                    principal.getName(), clientId, scope);

            // Armazena o ID do consentimento para uso posterior
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes()).getRequest();
            request.getSession().setAttribute("CONSENT_ID", consentId);
        }

        // Salva o consentimento no sistema OAuth2
        this.authorizationConsentService.save(consentBuilder.build());

        // Constrói a URL de redirecionamento para continuar o fluxo OAuth2
        String redirectUrl = "/oauth2/authorize?" +
                OAuth2ParameterNames.CLIENT_ID + "=" + clientId + "&" +
                OAuth2ParameterNames.SCOPE + "=" + scope + "&" +
                OAuth2ParameterNames.STATE + "=" + state;

        if (userCode != null) {
            redirectUrl += "&" + OAuth2ParameterNames.USER_CODE + "=" + userCode;
        }

        return "redirect:" + redirectUrl;
    }
}