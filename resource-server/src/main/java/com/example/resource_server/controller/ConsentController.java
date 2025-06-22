package com.example.resource_server.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.example.resource_server.model.Consent;
import com.example.resource_server.model.Transaction;
import com.example.resource_server.service.AuditService;
import com.example.resource_server.service.ConsentService;
import com.example.resource_server.service.TransactionService;

/**
 * Controlador para gerenciar consentimentos
 * No Open Finance, o consentimento é um conceito central que deve ser
 * gerenciado explicitamente
 */
@RestController
@RequestMapping("/api/consents")
public class ConsentController {

    private final ConsentService consentService;
    private final AuditService auditService;
    private final TransactionService transactionService;

    public ConsentController(ConsentService consentService, AuditService auditService,
            TransactionService transactionService) {
        this.consentService = consentService;
        this.auditService = auditService;
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Consent> createConsent(@RequestBody Set<String> permissions) {
        Jwt jwt = getJwtToken();
        String userId = jwt.getSubject();
        String clientId = jwt.getClaimAsString("client_id");

        try {
            Consent consent = consentService.createConsent(userId, clientId, permissions);
            auditService.logAction(jwt, "consent", consent.getConsentId(), "CREATE", "SUCCESS", null);
            return ResponseEntity.ok(consent);
        } catch (Exception e) {
            auditService.logAction(jwt, "consent", "new", "CREATE", "FAILED", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{consentId}/authorize")
    public ResponseEntity<Consent> authorizeConsent(@PathVariable String consentId) {
        Jwt jwt = getJwtToken();

        try {
            Consent consent = consentService.authorizeConsent(consentId);
            auditService.logAction(jwt, "consent", consentId, "AUTHORIZE", "SUCCESS", null);
            return ResponseEntity.ok(consent);
        } catch (Exception e) {
            auditService.logAction(jwt, "consent", consentId, "AUTHORIZE", "FAILED", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{consentId}/revoke")
    public ResponseEntity<Consent> revokeConsent(@PathVariable String consentId) {
        Jwt jwt = getJwtToken();

        try {
            Consent consent = consentService.revokeConsent(consentId);
            auditService.logAction(jwt, "consent", consentId, "REVOKE", "SUCCESS", null);
            return ResponseEntity.ok(consent);
        } catch (Exception e) {
            auditService.logAction(jwt, "consent", consentId, "REVOKE", "FAILED", e.getMessage());
            throw e;
        }
    }

    // Removed duplicate method getJwtToken()

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Obtém o token JWT
        Jwt jwt = getJwtToken();
        String username = jwt.getSubject();

        // Verifica o consentimento antes de fornecer acesso
        if (!consentService.validateConsent(jwt, "transactions")) {
            return ResponseEntity.status(403).build();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Transaction> transactions = transactionService.getTransactionsByDate(
                username, startDateTime, endDateTime);

        return ResponseEntity.ok(transactions);
    }

    private Jwt getJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Jwt) authentication.getPrincipal();
    }
}