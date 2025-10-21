package com.example.resource_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.example.resource_server.model.Transaction;
import com.example.resource_server.service.ConsentValidationService;
import com.example.resource_server.service.TransactionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/open-banking/accounts/v2")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ConsentValidationService consentValidationService;

    /**
     * Lista transações de uma conta
     * GET /accounts/{accountId}/transactions
     */
    @GetMapping("/accounts/{accountId}/transactions")
    @PreAuthorize("hasAuthority('SCOPE_accounts')")
    public ResponseEntity<Map<String, Object>> getTransactions(
            @PathVariable String accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int pageSize,
            @AuthenticationPrincipal Jwt jwt) {

        String consentId = extractConsentId(jwt);
        consentValidationService.validateConsentForResourceAccess(consentId, "ACCOUNTS_TRANSACTIONS_READ");

        String cpf = jwt.getClaimAsString("cpf");

        // Busca transações
        List<Transaction> transactions = transactionService.findTransactions(
                accountId,
                cpf,
                fromDate,
                toDate,
                page,
                pageSize);

        Map<String, Object> response = Map.of(
                "data", transactions,
                "links", Map.of(
                        "self", "/open-banking/accounts/v2/accounts/" + accountId + "/transactions",
                        "first", "/open-banking/accounts/v2/accounts/" + accountId + "/transactions?page=1",
                        "prev",
                        page > 1 ? "/open-banking/accounts/v2/accounts/" + accountId + "/transactions?page="
                                + (page - 1) : "",
                        "next", "/open-banking/accounts/v2/accounts/" + accountId + "/transactions?page=" + (page + 1)),
                "meta", Map.of(
                        "totalRecords", transactions.size(),
                        "totalPages", 1));

        return ResponseEntity.ok(response);
    }

    private String extractConsentId(Jwt jwt) {
        String scope = jwt.getClaimAsString("scope");
        for (String s : scope.split(" ")) {
            if (s.startsWith("consent:")) {
                return s.substring(8);
            }
        }
        throw new IllegalArgumentException("Consent ID não encontrado");
    }
}