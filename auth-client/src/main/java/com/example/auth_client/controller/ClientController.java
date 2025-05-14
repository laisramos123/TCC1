package com.example.auth_client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class ClientController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home(
            @AuthenticationPrincipal OAuth2User principal,
            Model model) {

        model.addAttribute("userName", principal.getAttribute("name"));
        model.addAttribute("userAttributes", principal.getAttributes());

        return "home";
    }

    @GetMapping("/profile")
    public String profile(
            @AuthenticationPrincipal OAuth2User principal,
            Model model) {

        model.addAttribute("userInfo", principal.getAttributes());

        return "profile";
    }

    // Este endpoint demonstra o uso do token para acessar APIs protegidas
    @GetMapping("/api-data")
    public String apiData(
            OAuth2AuthenticationToken authentication,
            Model model) {

        OAuth2AuthorizedClient client = getAuthorizedClient(authentication);

        // Exemplo de uso do token para chamar uma API protegida
        String apiResponse = callProtectedApi(client.getAccessToken().getTokenValue());
        model.addAttribute("apiResponse", apiResponse);

        return "api-data";
    }

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    private OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication) {
        return authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());
    }

    private String callProtectedApi(String accessToken) {
        // Implementação real usaria RestTemplate ou WebClient para chamar uma API
        // Aqui é apenas um exemplo
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>("", headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "http://localhost:9000/api/resource",
                    HttpMethod.GET,
                    entity,
                    String.class);

            return response.getBody();
        } catch (Exception e) {
            return "Erro ao chamar API: " + e.getMessage();
        }
    }
}
