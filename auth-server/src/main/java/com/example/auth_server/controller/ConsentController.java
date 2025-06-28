package com.example.auth_server.controller;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.auth_server.model.Consent;
import com.example.auth_server.service.ConsentService;
import com.example.auth_server.service.OAuth2ConsentIntegrator;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.security.Principal;

import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ConsentController {

    private final ConsentService consentService;
    private final OAuth2ConsentIntegrator oauth2ConsentIntegrator;
    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationService authorizationService;

    @GetMapping("/oauth2/consent")
    public String consent(
            Principal principal,
            Model model,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "response_type", required = false) String responseType,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod,
            HttpServletRequest request) {

        try {
            log.info(" Pagina de consentimento acessada por: {} para cliente: {}",
                    principal.getName(), clientId);

            log.info(" Parametros OAuth2 recebidos:");
            log.info("   - client_id: {}", clientId);
            log.info("   - scope: {}", scope);
            log.info("   - state: {}", state);
            log.info("   - response_type: {}", responseType);
            log.info("   - redirect_uri: {}", redirectUri);
            log.info("   - code_challenge: {}", codeChallenge);
            log.info("   - code_challenge_method: {}", codeChallengeMethod);

            if (clientId == null || scope == null) {
                log.error("  Parametros obrigatorios ausentes: clientId={}, scope={}", clientId, scope);
                model.addAttribute("error", "Parâmetros de autorização inválidos");
                return "error";
            }

            OAuth2AuthorizationRequest authorizationRequest = findPendingAuthorizationRequest(principal.getName(),
                    clientId, state);

            if (authorizationRequest != null) {
                log.info("  Parametros OAuth2 encontrados na sessão:");

                if (responseType == null) {
                    responseType = authorizationRequest.getResponseType().getValue();
                    log.info("     response_type recuperado: {}", responseType);
                }

                if (redirectUri == null) {
                    redirectUri = authorizationRequest.getRedirectUri();
                    log.info("      redirect_uri recuperado: {}", redirectUri);
                }

                if (codeChallenge == null) {
                    codeChallenge = authorizationRequest.getAttribute("code_challenge");
                    log.info("     code_challenge recuperado: {}", codeChallenge);
                }

                if (codeChallengeMethod == null) {
                    codeChallengeMethod = authorizationRequest.getAttribute("code_challenge_method");
                    log.info("     code_challenge_method recuperado: {}", codeChallengeMethod);
                }
            }

            if (responseType == null) {
                responseType = "code";
                log.info("  Usando response_type padrão: {}", responseType);
            }

            if (redirectUri == null) {
                RegisteredClient client = registeredClientRepository.findByClientId(clientId);
                if (client != null && !client.getRedirectUris().isEmpty()) {
                    redirectUri = client.getRedirectUris().iterator().next();
                    log.info("  redirect_uri obtido do cliente registrado: {}", redirectUri);
                } else {
                    redirectUri = "http://localhost:8081/login/oauth2/code/tpp-client";
                    log.info("  Usando redirect_uri padrão: {}", redirectUri);
                }
            }

            Set<String> permissions = Set.of(scope.split(" "));

            String userId = principal.getName();
            Consent existingConsent = consentService.findLatestPendingConsent(userId, clientId);

            if (existingConsent == null) {

                existingConsent = consentService.createConsent(userId, clientId, permissions);
                log.info("  Novo consentimento criado: {}", existingConsent.getConsentId());
            } else {
                log.info("  Reutilizando consentimento existente: {}", existingConsent.getConsentId());
            }

            model.addAttribute("clientId", clientId);
            model.addAttribute("clientName", "TPP Financial Services");
            model.addAttribute("clientCnpj", "12.345.678/0001-90");
            model.addAttribute("permissions", permissions);
            model.addAttribute("scope", scope);
            model.addAttribute("state", state);
            model.addAttribute("consentId", existingConsent.getConsentId());
            model.addAttribute("consentExpirationDate",
                    existingConsent.getExpiresAt().toLocalDate().toString());

            model.addAttribute("responseType", responseType);
            model.addAttribute("redirectUri", redirectUri);
            model.addAttribute("codeChallenge", codeChallenge);
            model.addAttribute("codeChallengeMethod", codeChallengeMethod);

            log.info("  Parametros OAuth2 finais enviados para view:");
            log.info("   - response_type: {}", responseType);
            log.info("   - redirect_uri: {}", redirectUri);
            log.info("   - code_challenge: {}", codeChallenge);
            log.info("   - code_challenge_method: {}", codeChallengeMethod);

            return "consent";

        } catch (Exception e) {
            log.error("  Erro ao processar pagina de consentimento", e);
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
            @RequestParam("scope") String scope,
            @RequestParam(value = "response_type", required = false) String responseType,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod) {

        try {
            String userId = principal.getName();
            log.info("  Processando acao de consentimento: {} por usuário: {} para cliente: {}",
                    action, userId, clientId);

            // Buscar consentimento pendente
            Consent consent = consentService.findLatestPendingConsent(userId, clientId);

            if (consent == null) {
                log.error("  Nenhum consentimento pendente encontrado");
                return "redirect:/oauth2/consent?error=consent_not_found";
            }

            if ("approve".equals(action)) {
                consentService.approveConsent(consent.getConsentId());
                log.info("✅ Consentimento Open Finance aprovado: {}", consent.getConsentId());

                Set<String> permissions = Set.of(scope.split(" "));
                oauth2ConsentIntegrator.integrateConsentApproval(userId, clientId, permissions);

                return buildAuthorizationRedirect(clientId, responseType, redirectUri, scope, state,
                        codeChallenge, codeChallengeMethod);

            } else if ("deny".equals(action)) {
                consentService.denyConsent(consent.getConsentId());
                log.info(" Consentimento negado: {}", consent.getConsentId());

                return redirectWithError(redirectUri, state);

            } else {
                log.error(" Acao invalida: {}", action);
                return "redirect:/oauth2/consent?error=invalid_action";
            }

        } catch (Exception e) {
            log.error(" Erro ao processar consentimento", e);
            return "redirect:/oauth2/consent?error=server_error";
        }
    }

    private OAuth2AuthorizationRequest findPendingAuthorizationRequest(String principalName, String clientId,
            String state) {
        try {
            log.debug(" Buscando OAuth2Authorization para: principalName={}, clientId={}, state={}",
                    principalName, clientId, state);

            if (state != null) {
                log.debug(" Tentando encontrar authorization request por state: {}", state);
            }

            return null;

        } catch (Exception e) {
            log.warn("  Erro ao buscar OAuth2AuthorizationRequest", e);
            return null;
        }
    }

    private String buildAuthorizationRedirect(String clientId, String responseType, String redirectUri,
            String scope, String state, String codeChallenge,
            String codeChallengeMethod) {
        try {
            StringBuilder url = new StringBuilder("/oauth2/authorize");
            url.append("?client_id=").append(URLEncoder.encode(clientId, "UTF-8"));
            url.append("&response_type=")
                    .append(URLEncoder.encode(responseType != null ? responseType : "code", "UTF-8"));
            url.append("&scope=").append(URLEncoder.encode(scope, "UTF-8"));

            if (redirectUri != null) {
                url.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
            }
            if (state != null) {
                url.append("&state=").append(URLEncoder.encode(state, "UTF-8"));
            }
            if (codeChallenge != null) {
                url.append("&code_challenge=").append(URLEncoder.encode(codeChallenge, "UTF-8"));
            }
            if (codeChallengeMethod != null) {
                url.append("&code_challenge_method=").append(URLEncoder.encode(codeChallengeMethod, "UTF-8"));
            }

            log.info(" Redirecionando para authorization endpoint: {}", url.toString());
            return "redirect:" + url.toString();

        } catch (Exception e) {
            log.error(" Erro ao construir URL de redirecionamento", e);
            return "redirect:/oauth2/consent?error=redirect_error";
        }
    }

    private String redirectWithError(String redirectUri, String state) {
        try {
            String defaultRedirectUri = "http://localhost:8081/login/oauth2/code/tpp-client";
            String finalRedirectUri = redirectUri != null ? redirectUri : defaultRedirectUri;

            StringBuilder url = new StringBuilder(finalRedirectUri);
            url.append("?error=access_denied");

            if (state != null) {
                url.append("&state=").append(URLEncoder.encode(state, "UTF-8"));
            }

            log.info(" Redirecionando com erro para: {}", url.toString());
            return "redirect:" + url.toString();

        } catch (Exception e) {
            log.error(" Erro ao construir URL de erro", e);
            return "redirect:/oauth2/consent?error=redirect_error";
        }
    }
}