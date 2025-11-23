package com.example.auth_client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.auth_client.dto.OpenBankingAccountResponse;
import com.example.auth_client.dto.TokenResponse;
import com.example.auth_client.service.AccountService;
import com.example.auth_client.service.TokenService;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TokenService tokenService;

    @GetMapping
    public String listAccounts(Model model, HttpSession session) {

        try {

            String accessToken = getValidAccessToken(session);

            OpenBankingAccountResponse accounts = accountService.getAccounts(accessToken);

            model.addAttribute("accounts", accounts.getData());
            model.addAttribute("success", true);

            return "accounts";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/{accountId}")
    public String accountDetails(
            @PathVariable String accountId,
            Model model,
            HttpSession session) {

        try {
            String accessToken = getValidAccessToken(session);

            Object balance = accountService.getAccountBalance(accountId, accessToken);

            model.addAttribute("accountId", accountId);
            model.addAttribute("balance", balance);

            return "account-details";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    private String getValidAccessToken(HttpSession session) {

        String accessToken = (String) session.getAttribute("access_token");
        Instant expiresAt = (Instant) session.getAttribute("token_expires_at");

        if (expiresAt != null && Instant.now().isAfter(expiresAt)) {

            String refreshToken = (String) session.getAttribute("refresh_token");

            if (refreshToken == null) {
                throw new RuntimeException("Refresh token não encontrado. Faça login novamente.");
            }

            TokenResponse newTokens = tokenService.refreshToken(refreshToken);

            accessToken = newTokens.getAccessToken();
            session.setAttribute("access_token", accessToken);
            session.setAttribute("refresh_token", newTokens.getRefreshToken());
            session.setAttribute("token_expires_at",
                    Instant.now().plusSeconds(newTokens.getExpiresIn()));
        }

        return accessToken;
    }
}
