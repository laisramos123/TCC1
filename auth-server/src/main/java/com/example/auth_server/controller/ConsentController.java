package com.example.auth_server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.auth_server.model.Consent;
import com.example.auth_server.service.ConsentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ConsentController {

    private final ConsentService consentService;

    @GetMapping("/oauth2/consent")
    public String consent(
            Principal principal,
            Model model,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state) {

        try {
            log.info(" Pagina de consentimento acessada por: {} para cliente: {}",
                    principal.getName(), clientId);

            // Validar parametros obrigatórios
            if (clientId == null || scope == null) {
                log.error(" Parametros obrigatórios ausentes: clientId={}, scope={}", clientId, scope);
                model.addAttribute("error", "Parametros de autorizaçao invalidos");
                return "error";
            }

            // Processar scopes
            Set<String> permissions = Set.of(scope.split(" "));

            // Verificar se existe consentimento pendente
            String userId = principal.getName();
            Consent existingConsent = consentService.findLatestPendingConsent(userId, clientId);

            if (existingConsent == null) {
                // Criar novo consentimento
                existingConsent = consentService.createConsent(userId, clientId, permissions);
                log.info(" Novo consentimento criado: {}", existingConsent.getConsentId());
            } else {
                log.info(" Reutilizando consentimento existente: {}", existingConsent.getConsentId());
            }

            // Preparar dados para a view
            model.addAttribute("clientId", clientId);
            model.addAttribute("clientName", "TPP Financial Services");
            model.addAttribute("clientCnpj", "12.345.678/0001-90");
            model.addAttribute("permissions", permissions);
            model.addAttribute("scope", scope);
            model.addAttribute("state", state);
            model.addAttribute("consentId", existingConsent.getConsentId());
            model.addAttribute("consentExpirationDate",
                    existingConsent.getExpiresAt().toLocalDate().toString());

            return "consent";

        } catch (Exception e) {
            log.error(" Erro ao processar pagina de consentimento", e);
            model.addAttribute("error", "Erro interno do servidor");
            return "error";
        }
    }

    @PostMapping("/oauth2/consent")
    public String processConsent(
            Principal principal,
            @RequestParam("action") String action,
            @RequestParam("client_id") String clientId,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam("scope") String scope) {

        try {
            String userId = principal.getName();
            log.info(" Processando açao de consentimento: {} por usuario: {} para cliente: {}",
                    action, userId, clientId);

            // Buscar consentimento pendente
            Consent consent = consentService.findLatestPendingConsent(userId, clientId);

            if (consent == null) {
                log.error(" Nenhum consentimento pendente encontrado");
                return "redirect:/oauth2/consent?error=consent_not_found";
            }

            String redirectUri = "http://localhost:8081/login/oauth2/code/tpp-client";

            if ("approve".equals(action)) {
                // Aprovar consentimento
                consentService.approveConsent(consent.getConsentId());
                log.info(" Consentimento aprovado: {}", consent.getConsentId());

                // Preparar parametros de redirecionamento
                String authCode = generateAuthorizationCode();
                String redirect = redirectUri + "?code=" + authCode;

                if (state != null) {
                    redirect += "&state=" + state;
                }

                return "redirect:" + redirect;

            } else if ("deny".equals(action)) {
                // Negar consentimento
                consentService.denyConsent(consent.getConsentId());
                log.info("Consentimento negado: {}", consent.getConsentId());

                // Redirecionar com erro
                String redirect = redirectUri + "?error=access_denied";
                if (state != null) {
                    redirect += "&state=" + state;
                }

                return "redirect:" + redirect;

            } else {
                log.error(" Açao invalida: {}", action);
                return "redirect:/oauth2/consent?error=invalid_action";
            }

        } catch (Exception e) {
            log.error(" Erro ao processar consentimento", e);
            return "redirect:/oauth2/consent?error=server_error";
        }
    }

    private String generateAuthorizationCode() {
        return "auth_code_" + java.util.UUID.randomUUID().toString().replace("-", "");
    }
}