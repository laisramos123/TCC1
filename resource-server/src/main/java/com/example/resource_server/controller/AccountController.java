package com.example.resource_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.example.resource_server.dto.AccountResponse;
import com.example.resource_server.service.AccountService;

import java.util.Map;

@RestController
@RequestMapping("/open-banking/accounts/v2")
public class AccountController {

        @Autowired
        private AccountService accountService;

        @GetMapping("/accounts")
        @PreAuthorize("hasAuthority('SCOPE_accounts')")
        public ResponseEntity<AccountResponse> getAccounts(
                        @AuthenticationPrincipal Jwt jwt,
                        @RequestHeader("x-fapi-interaction-id") String interactionId) {

                String cpf = jwt.getClaimAsString("cpf");

                AccountResponse response = accountService.getAccounts(cpf);

                return ResponseEntity
                                .ok()
                                .header("x-fapi-interaction-id", interactionId)
                                .body(response);
        }

        @GetMapping("/accounts/{accountId}")
        @PreAuthorize("hasAuthority('SCOPE_accounts')")
        public ResponseEntity<AccountResponse.AccountData> getAccountById(
                        @PathVariable String accountId,
                        @AuthenticationPrincipal Jwt jwt,
                        @RequestHeader("x-fapi-interaction-id") String interactionId) {

                String cpf = jwt.getClaimAsString("cpf");

                AccountResponse.AccountData account = accountService.getAccountById(accountId, cpf);

                return ResponseEntity
                                .ok()
                                .header("x-fapi-interaction-id", interactionId)
                                .body(account);
        }

        @GetMapping("/accounts/{accountId}/balances")
        @PreAuthorize("hasAuthority('SCOPE_accounts')")
        public ResponseEntity<Map<String, Object>> getAccountBalance(
                        @PathVariable String accountId,
                        @AuthenticationPrincipal Jwt jwt,
                        @RequestHeader("x-fapi-interaction-id") String interactionId) {

                String cpf = jwt.getClaimAsString("cpf");

                Map<String, Object> balance = accountService.getAccountBalance(accountId, cpf);

                return ResponseEntity
                                .ok()
                                .header("x-fapi-interaction-id", interactionId)
                                .body(balance);
        }
}