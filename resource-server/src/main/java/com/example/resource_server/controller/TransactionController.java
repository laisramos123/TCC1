package com.example.resource_server.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.resource_server.exception.ConsentException;
import com.example.resource_server.model.Transaction;
import com.example.resource_server.service.AuditService;
import com.example.resource_server.service.ConsentService;
import com.example.resource_server.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final ConsentService consentService;
    private final AuditService auditService;

    public TransactionController(TransactionService transactionService,
            ConsentService consentService,
            AuditService auditService) {
        this.transactionService = transactionService;
        this.consentService = consentService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions() {
        // Obtém o token JWT
        Jwt jwt = getJwtToken();
        String username = jwt.getSubject();

        try {
            // Verifica o consentimento antes de fornecer acesso
            if (!consentService.validateConsent(jwt, "transactions")) {
                auditService.logAction(jwt, "transactions", "all", "READ", "FAILED", "Consentimento inválido");
                throw new ConsentException("Consentimento inválido ou expirado para acesso às transações");
            }

            List<Transaction> transactions = transactionService.getTransactionsByOwner(username);

            // Registra a ação bem-sucedida
            auditService.logAction(jwt, "transactions", "all", "READ", "SUCCESS", null);

            return ResponseEntity.ok(transactions);
        } catch (ConsentException e) {
            throw e;
        } catch (Exception e) {
            // Registra falha e relança a exceção
            auditService.logAction(jwt, "transactions", "all", "READ", "FAILED", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Transaction>> getTransactionsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Obtém o token JWT
        Jwt jwt = getJwtToken();
        String username = jwt.getSubject();
        String filterDesc = "date-filter:" + startDate + "-" + endDate;

        try {
            // Verifica o consentimento antes de fornecer acesso
            if (!consentService.validateConsent(jwt, "transactions")) {
                auditService.logAction(jwt, "transactions", filterDesc, "READ", "FAILED", "Consentimento inválido");
                throw new ConsentException("Consentimento inválido ou expirado para acesso às transações");
            }

            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            List<Transaction> transactions = transactionService.getTransactionsByDate(
                    username, startDateTime, endDateTime);

            // Registra a ação bem-sucedida
            auditService.logAction(jwt, "transactions", filterDesc, "READ", "SUCCESS", null);

            return ResponseEntity.ok(transactions);
        } catch (ConsentException e) {
            throw e;
        } catch (Exception e) {
            // Registra falha e relança a exceção
            auditService.logAction(jwt, "transactions", filterDesc, "READ", "FAILED", e.getMessage());
            throw e;
        }
    }

    private Jwt getJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Jwt) authentication.getPrincipal();
    }
}
