package com.example.rest;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public ClientController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/request-token")
    public String requestToken() {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("my-client", "user");
        return client != null ? "Access Token: " + client.getAccessToken().getTokenValue() : "No token available";
    }
}
