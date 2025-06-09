package com.example.auth_server.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.auth_server.model.Consent;
import com.example.auth_server.service.ConsentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.*;

@Controller
public class AuthorizationController {

    private final RegisteredClientRepository clientRepository;
    private final ConsentService consentService;

    public AuthorizationController(RegisteredClientRepository clientRepository,
            ConsentService consentService) {
        this.clientRepository = clientRepository;
        this.consentService = consentService;
    }

    @GetMapping("/oauth2/consent")
    public String consentPage(Principal principal,
            HttpServletRequest request,
            HttpSession session,
            Model model) {

        // Capturar parâmetros da URL atual
        String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        String scope = request.getParameter(OAuth2ParameterNames.SCOPE);
        String state = request.getParameter(OAuth2ParameterNames.STATE);

        System.out.println(" CHEGOU NA TELA DE CONSENTIMENTO!");
        System.out.println("Client ID: " + clientId);
        System.out.println("Scope: " + scope);
        System.out.println("State: " + state);

        // DEBUG: Mostrar ID da sessão atual
        System.out.println("Session ID: " + session.getId());

        // DEBUG: Mostrar todos os atributos da sessão
        System.out.println("=== ATRIBUTOS DA SESSÃO ===");
        Enumeration<String> attrs = session.getAttributeNames();
        while (attrs.hasMoreElements()) {
            String name = attrs.nextElement();
            System.out.println(name + " = " + session.getAttribute(name));
        }
        System.out.println("========================");

        // Tentar recuperar parâmetros de várias fontes
        String redirectUri = null;
        String nonce = null;
        String responseType = "code"; // Padrão

        // 1. Primeiro, tentar da URL atual
        redirectUri = request.getParameter(OAuth2ParameterNames.REDIRECT_URI);
        nonce = request.getParameter("nonce");
        responseType = request.getParameter("response_type");

        // 2. Se não estiver na URL, tentar da SavedRequest
        if (redirectUri == null || nonce == null) {
            SavedRequest savedRequest = (SavedRequest) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
            if (savedRequest != null) {
                System.out.println("📌 SavedRequest encontrado: " + savedRequest.getRedirectUrl());
                Map<String, String[]> parameterMap = savedRequest.getParameterMap();

                if (redirectUri == null && parameterMap.get(OAuth2ParameterNames.REDIRECT_URI) != null) {
                    redirectUri = parameterMap.get(OAuth2ParameterNames.REDIRECT_URI)[0];
                }

                if (nonce == null && parameterMap.get("nonce") != null) {
                    nonce = parameterMap.get("nonce")[0];
                }

                if (responseType == null && parameterMap.get("response_type") != null) {
                    responseType = parameterMap.get("response_type")[0];
                }
            }
        }

        // 3. Se ainda não temos, tentar da sessão
        if (redirectUri == null) {
            redirectUri = (String) session.getAttribute("oauth2_redirect_uri");
        }
        if (nonce == null) {
            nonce = (String) session.getAttribute("oauth2_nonce");
        }
        if (responseType == null) {
            responseType = (String) session.getAttribute("oauth2_response_type");
        }

        // 4. Se AINDA não temos redirect_uri, buscar do cliente registrado
        if (redirectUri == null || redirectUri.trim().isEmpty()) {
            RegisteredClient client = clientRepository.findByClientId(clientId);
            if (client != null && !client.getRedirectUris().isEmpty()) {
                redirectUri = client.getRedirectUris().iterator().next();
                System.out.println("⚠️ Usando redirect_uri do cliente registrado: " + redirectUri);
            }
        }

        // 5. IMPORTANTE: Salvar TODOS os parâmetros na sessão para preservá-los
        session.setAttribute("oauth2_client_id", clientId);
        session.setAttribute("oauth2_redirect_uri", redirectUri);
        session.setAttribute("oauth2_response_type", responseType);
        session.setAttribute("oauth2_scope", scope);
        session.setAttribute("oauth2_state", state);
        if (nonce != null) {
            session.setAttribute("oauth2_nonce", nonce);
        }

        System.out.println("📋 Parâmetros salvos/recuperados:");
        System.out.println("Redirect URI: " + redirectUri);
        System.out.println("Nonce: " + nonce);
        System.out.println("Response Type: " + responseType);

        RegisteredClient client = clientRepository.findByClientId(clientId);
        if (client == null) {
            throw new RuntimeException("Cliente desconhecido: " + clientId);
        }

        Set<String> scopesToApprove = new HashSet<>();
        if (scope != null) {
            String[] scopes = scope.split(" ");
            for (String scopeValue : scopes) {
                scopesToApprove.add(scopeValue);
            }
        }

        // Cria um novo consentimento para o usuário
        Set<String> permissions = new HashSet<>(scopesToApprove);
        Consent consent = consentService.createConsent(principal.getName(), clientId, permissions);

        model.addAttribute("clientId", clientId);
        model.addAttribute("clientName", client.getClientName());
        model.addAttribute("scopes", scopesToApprove);
        model.addAttribute("state", state);
        model.addAttribute("consentId", consent.getConsentId());
        model.addAttribute("redirectUri", redirectUri);
        model.addAttribute("responseType", responseType);
        model.addAttribute("nonce", nonce);
        model.addAttribute("originalScope", scope);

        return "consent";
    }

    @PostMapping("/oauth2/consent")
    public String processConsent(
            @RequestParam("action") String action,
            @RequestParam("consentId") String consentId,
            Principal principal,
            HttpServletRequest request,
            HttpSession session) {

        System.out.println("🔄 PROCESSANDO CONSENTIMENTO!");
        System.out.println("Action: " + action);
        System.out.println("Consent ID: " + consentId);

        // 1. RECUPERAR parâmetros ORIGINAIS da SavedRequest
        SavedRequest savedRequest = (SavedRequest) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");

        String originalState = null;
        String originalRedirectUri = null;
        String originalClientId = null;
        String originalScope = null;
        String originalNonce = null;

        if (savedRequest != null) {
            Map<String, String[]> parameterMap = savedRequest.getParameterMap();
            originalState = getParameterValue(parameterMap, OAuth2ParameterNames.STATE);
            originalRedirectUri = getParameterValue(parameterMap, OAuth2ParameterNames.REDIRECT_URI);
            originalClientId = getParameterValue(parameterMap, OAuth2ParameterNames.CLIENT_ID);
            originalScope = getParameterValue(parameterMap, OAuth2ParameterNames.SCOPE);
            originalNonce = getParameterValue(parameterMap, "nonce");

            System.out.println("📌 Parâmetros originais da SavedRequest:");
            System.out.println("  - State: " + originalState);
            System.out.println("  - Client ID: " + originalClientId);
            System.out.println("  - Redirect URI: " + originalRedirectUri);
        } else {
            // Fallback: usar parâmetros da sessão
            System.out.println("⚠️ SavedRequest não encontrado, usando sessão");
            originalState = (String) session.getAttribute("oauth2_state");
            originalRedirectUri = (String) session.getAttribute("oauth2_redirect_uri");
            originalClientId = (String) session.getAttribute("oauth2_client_id");
            originalScope = (String) session.getAttribute("oauth2_scope");
            originalNonce = (String) session.getAttribute("oauth2_nonce");
        }

        // Validações básicas
        if (originalState == null || originalClientId == null) {
            throw new RuntimeException("Estado da sessão inválido - parâmetros obrigatórios ausentes");
        }

        try {
            if ("approve".equals(action)) {
                System.out.println("✅ Aprovando consentimento: " + consentId);

                // Aprovar o consentimento
                Consent consent = consentService.findByConsentId(consentId);
                if (consent == null) {
                    throw new RuntimeException("Consentimento não encontrado: " + consentId);
                }

                consentService.findByConsentId(consentId);

                // 2. IMPORTANTE: Limpar SavedRequest para evitar loops
                session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST");

                // 3. Construir URL de redirecionamento com parâmetros ORIGINAIS
                String authUrl = buildAuthorizationUrl(originalClientId, originalState,
                        originalRedirectUri, originalScope, originalNonce);

                System.out.println("🔄 Redirecionando para: " + authUrl);
                return "redirect:" + authUrl;

            } else if ("deny".equals(action)) {
                System.out.println("❌ Negando consentimento: " + consentId);

                // Negar o consentimento
                consentService.denyConsent(consentId);

                // Redirecionar para cliente com erro
                String errorUrl = originalRedirectUri +
                        "?error=access_denied" +
                        "&error_description=User+denied+the+request" +
                        "&state=" + URLEncoder.encode(originalState, StandardCharsets.UTF_8);

                return "redirect:" + errorUrl;
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao processar consentimento: " + e.getMessage());
            e.printStackTrace();

            // Em caso de erro, redirecionar com erro
            if (originalRedirectUri != null && originalState != null) {
                String errorUrl = originalRedirectUri +
                        "?error=server_error" +
                        "&error_description=Internal+server+error" +
                        "&state=" + URLEncoder.encode(originalState, StandardCharsets.UTF_8);

                return "redirect:" + errorUrl;
            }
        }

        return "redirect:/oauth2/error";
    }

    // Método auxiliar para construir URL de autorização
    private String buildAuthorizationUrl(String clientId, String state, String redirectUri,
            String scope, String nonce) {

        StringBuilder url = new StringBuilder("/oauth2/authorize");
        url.append("?response_type=code");
        url.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
        url.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        url.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));

        if (scope != null && !scope.trim().isEmpty()) {
            url.append("&scope=").append(URLEncoder.encode(scope, StandardCharsets.UTF_8));
        }

        if (nonce != null && !nonce.trim().isEmpty()) {
            url.append("&nonce=").append(URLEncoder.encode(nonce, StandardCharsets.UTF_8));
        }

        return url.toString();
    }

    // Método auxiliar reutilizado
    private String getParameterValue(Map<String, String[]> parameterMap, String parameterName) {
        String[] values = parameterMap.get(parameterName);
        return (values != null && values.length > 0) ? values[0] : null;
    }
}