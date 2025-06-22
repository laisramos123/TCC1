package com.example.resource_server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.resource_server.exception.ConsentException;
import com.example.resource_server.model.Account;
import com.example.resource_server.service.AccountService;
import com.example.resource_server.service.AuditService;
import com.example.resource_server.service.ConsentService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final ConsentService consentService;
    private final AuditService auditService;

    public AccountController(AccountService accountService,
            ConsentService consentService,
            AuditService auditService) {
        this.accountService = accountService;
        this.consentService = consentService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAccounts() {
        // Obtém o token JWT
        Jwt jwt = getJwtToken();
        String username = jwt.getSubject();

        try {
            // Verifica o consentimento antes de fornecer acesso
            if (!consentService.validateConsent(jwt, "accounts")) {
                auditService.logAction(jwt, "accounts", "all", "READ", "FAILED", "Consentimento inválido");
                throw new ConsentException("Consentimento inválido ou expirado para acesso às contas");
            }

            // Usa o serviço para buscar as contas do usuário
            List<Account> accounts = accountService.getAccountsByOwner(username);

            // Registra a ação bem-sucedida
            auditService.logAction(jwt, "accounts", "all", "READ", "SUCCESS", null);

            return ResponseEntity.ok(accounts);
        } catch (ConsentException e) {
            throw e;
        } catch (Exception e) {
            // Registra falha e relança a exceção
            auditService.logAction(jwt, "accounts", "all", "READ", "FAILED", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        // Obtém o token JWT
        Jwt jwt = getJwtToken();
        String username = jwt.getSubject();

        try {
            // Verifica o consentimento antes de fornecer acesso
            if (!consentService.validateConsent(jwt, "accounts")) {
                auditService.logAction(jwt, "accounts", id.toString(), "READ", "FAILED", "Consentimento inválido");
                throw new ConsentException("Consentimento inválido ou expirado para acesso às contas");
            }

            Account account = accountService.getAccountById(id, username);

            // Registra a ação bem-sucedida
            auditService.logAction(jwt, "accounts", id.toString(), "READ", "SUCCESS", null);

            return ResponseEntity.ok(account);
        } catch (ConsentException e) {
            throw e;
        } catch (Exception e) {
            // Registra falha e relança a exceção
            auditService.logAction(jwt, "accounts", id.toString(), "READ", "FAILED", e.getMessage());
            throw e;
        }
    }

    private Jwt getJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Jwt) authentication.getPrincipal();
    }
}