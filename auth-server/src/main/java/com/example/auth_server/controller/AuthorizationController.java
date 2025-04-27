package com.example.auth_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.example.auth_server.service.ConsentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class AuthorizationController {
    @Autowired
    private ConsentService consentService;;

    @GetMapping("/oauth/authorize")
    public ModelAndView authorizeRequest(
            @RequestParam("response_type") String responseType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("scope") String scope,
            @RequestParam("state") String state,
            @RequestParam(value = "nounce", required = false) String nounce,
            @RequestParam(value = "request", required = false) String request)
            throws Exception {

        // TODO: Verificar se o cliente é válido
        // TODO: Validar o redirect-uri
        // TODO: Verificar se o request JWT é válido(no caso do FAPI)
        // TODO: Decodificar o requestJWT para extrair claims

        // String consentId = consentService.createConsent(clientId, redirectUri,
        // scope,state, nounce, request);

        ModelAndView modelAndView = new ModelAndView("authorization");
        modelAndView.addObject("clientId", clientId);
        modelAndView.addObject("redirectUri", redirectUri);
        modelAndView.addObject("scope", scope);
        modelAndView.addObject("state", state);
        modelAndView.addObject("nounce", nounce);
        modelAndView.addObject("request", request);
        // modelAndView.addObject("consentId", consentId);
        modelAndView.addObject("responseType", responseType);
        modelAndView.addObject("clientName", "Client Name"); // TODO: Obter o nome do cliente a partir do clientId
        modelAndView.addObject("redirectUri", redirectUri);
        return modelAndView;

    }

    @PostMapping("/oauth/authorize/decision")
    public String processAuthorization(
            @RequestParam("user_id") String userId,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("state") String state,
            @RequestParam("consent_id") String consentId,
            @RequestParam("approved") boolean approved) {
        if (approved) {
            // consentService.approveConsent(consentId, userId);
            String authCode = generateAuthorizationCode(consentId, userId, clientId);
            return "redirect:" + redirectUri + "?code=" + authCode + "&state=" + state;
        } else {
            // consentService.rejectConsent(consentId, userId);
            return "redirect:" + redirectUri + "?error=access_denied&state=" + state;
        }
    }

    private String generateAuthorizationCode(String consentId, String userId, String clientId) {
        // TODO: Implementar a lógica para gerar o código de autorização
        // Isso pode incluir a criação de um JWT ou outro tipo de token
        // que contenha informações sobre o consentimento e o usuário.
        // O código de autorização deve ser único e ter um tempo de expiração.
        // Aqui, você pode usar uma biblioteca JWT para criar o token.
        // Exemplo: return jwtService.createAuthorizationCode(consentId, userId,
        // clientId);
        return "auth_code_" + System.currentTimeMillis();
    }

}
