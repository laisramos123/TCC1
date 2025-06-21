package com.example.auth_client.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.auth_client.model.Account;
import com.example.auth_client.model.Transaction;
import com.example.auth_client.service.ResourceServerService;

@Controller
public class HomeController {

    private final ResourceServerService resourceServerService;

    public HomeController(ResourceServerService resourceServerService) {
        this.resourceServerService = resourceServerService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User principal, Model model) {
        model.addAttribute("name", principal.getAttribute("name"));
        return "dashboard";
    }

    @GetMapping("/accounts")
    public String accounts(
            @RegisteredOAuth2AuthorizedClient("tpp-client") OAuth2AuthorizedClient authorizedClient,
            Model model) {

        try {
            List<Account> accounts = resourceServerService.getAccounts(authorizedClient);
            model.addAttribute("accounts", accounts);
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("success", false);
        }

        return "accounts";
    }

    @GetMapping("/accounts/{id}")
    public String accountDetails(
            @PathVariable Long id,
            @RegisteredOAuth2AuthorizedClient("tpp-client") OAuth2AuthorizedClient authorizedClient,
            Model model) {

        try {
            Account account = resourceServerService.getAccountById(authorizedClient, id);
            model.addAttribute("account", account);
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("success", false);
        }

        return "account-details";
    }

    @GetMapping("/transactions")
    public String transactions(
            @RegisteredOAuth2AuthorizedClient("tpp-client") OAuth2AuthorizedClient authorizedClient,
            Model model) {

        try {
            List<Transaction> transactions = resourceServerService.getTransactions(authorizedClient);
            model.addAttribute("transactions", transactions);
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("success", false);
        }

        return "transactions";
    }

    @GetMapping("/transactions/filter")
    public String transactionsFilter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RegisteredOAuth2AuthorizedClient("tpp-client") OAuth2AuthorizedClient authorizedClient,
            Model model) {

        try {
            List<Transaction> transactions = resourceServerService.getTransactionsByDate(
                    authorizedClient, startDate, endDate);
            model.addAttribute("transactions", transactions);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("success", false);
        }

        return "transactions";
    }
}