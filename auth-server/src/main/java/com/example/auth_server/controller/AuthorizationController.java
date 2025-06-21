package com.example.auth_server.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.auth_server.model.Consent;
import com.example.auth_server.service.ConsentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthorizationController {

    private final RegisteredClientRepository clientRepository;
    private final ConsentService consentService;

    private static final Map<String, String> PERMISSION_DESCRIPTIONS = new HashMap<>();

    static {
        PERMISSION_DESCRIPTIONS.put("accounts", "Dados de Contas");
        PERMISSION_DESCRIPTIONS.put("credit-cards-accounts", "Dados de Cartões de Crédito");
        PERMISSION_DESCRIPTIONS.put("loans", "Dados de Empréstimos");
        PERMISSION_DESCRIPTIONS.put("financings", "Dados de Financiamentos");
        PERMISSION_DESCRIPTIONS.put("invoice-financings", "Antecipação de Recebíveis");
        PERMISSION_DESCRIPTIONS.put("unarranged-accounts-overdraft", "Adiantamento a Depositantes");
        PERMISSION_DESCRIPTIONS.put("openid", "Identificação Básica");
        PERMISSION_DESCRIPTIONS.put("profile", "Perfil do Usuário");
        PERMISSION_DESCRIPTIONS.put("email", "Endereço de E-mail");
    }

    @GetMapping("/oauth2/consent")
    public String displayConsent(
            Principal principal,
            Model model,
            @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
            @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
            @RequestParam(OAuth2ParameterNames.STATE) String state,
            @RequestParam(value = OAuth2ParameterNames.REDIRECT_URI, required = false) String redirectUri,
            @RequestParam(value = OAuth2ParameterNames.RESPONSE_TYPE, required = false) String responseType,
            @RequestParam(value = OAuth2ParameterNames.NONCE, required = false) String nonce,
            HttpSession session) {

        log.info("🏦 Exibindo tela de consentimento Open Finance");
        log.info("Cliente: {}, Usuário: {}, Scopes: {}", clientId, principal.getName(), scope);

        // Validar cliente
        RegisteredClient registeredClient = clientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            log.error("❌ Cliente não encontrado: {}", clientId);
            return "error";
        }

        // Salvar parâmetros OAuth2 na sessão para uso posterior
        session.setAttribute("oauth2_client_id", clientId);
        session.setAttribute("oauth2_scope", scope);
        session.setAttribute("oauth2_state", state);
        session.setAttribute("oauth2_redirect_uri", redirectUri);
        session.setAttribute("oauth2_response_type", responseType);
        session.setAttribute("oauth2_nonce", nonce);

        // Processar permissões
        Set<String> requestedScopes = Arrays.stream(scope.split("\\s+"))
                .collect(Collectors.toSet());

        // Filtrar apenas permissões do Open Finance (não incluir openid, profile, email
        // na tela)
        Set<String> openFinancePermissions = requestedScopes.stream()
                .filter(s -> !s.equals("openid") && !s.equals("profile") && !s.equals("email"))
                .collect(Collectors.toSet());

        // Criar consentimento pendente
        String consentId = UUID.randomUUID().toString();
        Consent consent = new Consent();
        consent.setConsentId(consentId);
        consent.setUserId(principal.getName());
        consent.setClient_id(clientId);
        consent.setPermissions(requestedScopes);
        consent.setStatus("AWAITING_AUTHORISATION");
        consent.setCreatedAt(LocalDateTime.now());
        consent.setExpiresAt(LocalDateTime.now().plusMonths(12)); // 12 meses conforme Open Finance

        consentService.save(consent);

        // Adicionar dados ao modelo
        model.addAttribute("consentId", consentId);
        model.addAttribute("client_id", clientId);
        model.addAttribute("clientName", registeredClient.getClientName());
        model.addAttribute("clientCnpj", "12.345.678/0001-90"); // Em produção, buscar do registro do cliente
        model.addAttribute("permissions", openFinancePermissions);
        model.addAttribute("permissionDescriptions", PERMISSION_DESCRIPTIONS);
        model.addAttribute("scope", scope);
        model.addAttribute("state", state);
        model.addAttribute("consentExpirationDate",
                consent.getExpiresAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Informações para o usuário
        model.addAttribute("userName", principal.getName());
        model.addAttribute("institutionName", "Banco Digital S.A."); // Nome da instituição transmissora

        return "consent";
    }

    @PostMapping("/oauth2/consent")
    public String processConsent(
            @RequestParam("action") String action,
            @RequestParam("client_id") String clientId,
            @RequestParam("state") String state,
            @RequestParam("scope") String scope,
            Principal principal,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session) {

        log.info("🔄 Processando consentimento: action={}, client={}, user={}",
                action, clientId, principal.getName());

        try {
            // Recuperar parâmetros da sessão
            String originalScope = (String) session.getAttribute("oauth2_scope");
            String redirectUri = (String) session.getAttribute("oauth2_redirect_uri");
            String responseType = (String) session.getAttribute("oauth2_response_type");
            String nonce = (String) session.getAttribute("oauth2_nonce");

            // Buscar o consentimento mais recente pendente
            Consent consent = consentService.findLatestPendingConsent(
                    principal.getName(), clientId);

            if (consent == null) {
                log.error("❌ Consentimento não encontrado");
                return "error";
            }

            if ("approve".equalsIgnoreCase(action)) {
                // Aprovar consentimento
                consent.setStatus("AUTHORISED");
                consentService.save(consent);

                log.info("✅ Consentimento aprovado: {}", consent.getConsentId());

                // Criar o token de autorização e continuar o fluxo OAuth2
                // Em produção, isso seria integrado com o Spring Authorization Server

                // Por agora, redirecionar de volta para o authorization endpoint
                String authorizationUrl = String.format(
                        "/oauth2/authorize?client_id=%s&response_type=%s&scope=%s&state=%s&redirect_uri=%s",
                        clientId, responseType, originalScope, state, redirectUri);

                if (nonce != null) {
                    authorizationUrl += "&nonce=" + nonce;
                }

                // Adicionar flag para indicar que o consentimento já foi processado
                session.setAttribute("consent_approved_" + clientId, true);

                return "redirect:" + authorizationUrl;

            } else {
                // Negar consentimento
                consent.setStatus("REJECTED");
                consent.setRevokedAt(LocalDateTime.now());
                consentService.save(consent);

                log.info("❌ Consentimento negado: {}", consent.getConsentId());

                // Redirecionar com erro
                String errorUri = redirectUri + "?error=access_denied&error_description=User+denied+consent";
                if (state != null) {
                    errorUri += "&state=" + state;
                }

                return "redirect:" + errorUri;
            }

        } catch (Exception e) {
            log.error("❌ Erro ao processar consentimento", e);
            return "error";
        } finally {
            // Limpar dados da sessão
            session.removeAttribute("oauth2_client_id");
            session.removeAttribute("oauth2_scope");
            session.removeAttribute("oauth2_state");
            session.removeAttribute("oauth2_redirect_uri");
            session.removeAttribute("oauth2_response_type");
            session.removeAttribute("oauth2_nonce");
        }
    }

    /**
     * Endpoint para revogar consentimentos (requisito Open Finance)
     */
    @PostMapping("/api/consents/{consentId}/revoke")
    @ResponseBody
    public Map<String, Object> revokeConsent(
            @PathVariable String consentId,
            Principal principal) {

        log.info("🚫 Revogando consentimento: {} para usuário: {}", consentId, principal.getName());

        Map<String, Object> response = new HashMap<>();

        try {
            Consent consent = consentService.findByConsentId(consentId);

            if (consent == null || !consent.getUserId().equals(principal.getName())) {
                response.put("status", "error");
                response.put("message", "Consentimento não encontrado");
                return response;
            }

            consent.setStatus("REVOKED");
            consent.setRevokedAt(LocalDateTime.now());
            consentService.save(consent);

            response.put("status", "success");
            response.put("message", "Consentimento revogado com sucesso");
            response.put("revokedAt", consent.getRevokedAt());

            log.info("✅ Consentimento revogado com sucesso: {}", consentId);

        } catch (Exception e) {
            log.error("❌ Erro ao revogar consentimento", e);
            response.put("status", "error");
            response.put("message", "Erro ao processar revogação");
        }

        return response;
    }

    /**
     * Endpoint para listar consentimentos do usuário
     */
    @GetMapping("/api/consents")
    @ResponseBody
    public Map<String, Object> listUserConsents(Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Consent> consents = consentService.findUserConsents(principal.getName());
            response.put("status", "success");
            response.put("consents", consents);
            response.put("total", consents.size());
        } catch (Exception e) {
            log.error("❌ Erro ao listar consentimentos", e);
            response.put("status", "error");
            response.put("message", "Erro ao buscar consentimentos");
        }

        return response;
    }
}