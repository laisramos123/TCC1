package com.example.auth_client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.example.auth_client.dto.TokenResponse;
import com.example.auth_client.service.TokenService;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;

@Controller
public class CallbackController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/callback")
    public RedirectView handleCallback(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String error,
            HttpSession session) {

        if (error != null) {
            return new RedirectView("/error?message=" + error);
        }

        try {

            String storedState = (String) session.getAttribute("state");
            String codeVerifier = (String) session.getAttribute("code_verifier");
            String consentId = (String) session.getAttribute("consent_id");

            if (!state.equals(storedState)) {
                return new RedirectView("/error?message=Invalid state");
            }

            TokenResponse tokenResponse = tokenService.exchangeCodeForToken(
                    code,
                    codeVerifier);

            session.setAttribute("access_token", tokenResponse.getAccessToken());
            session.setAttribute("refresh_token", tokenResponse.getRefreshToken());
            session.setAttribute("token_expires_at",
                    Instant.now().plusSeconds(tokenResponse.getExpiresIn()));

            session.removeAttribute("code_verifier");
            session.removeAttribute("state");

            return new RedirectView("/accounts");

        } catch (Exception e) {
            e.printStackTrace();
            return new RedirectView("/error?message=" + e.getMessage());
        }
    }
}
