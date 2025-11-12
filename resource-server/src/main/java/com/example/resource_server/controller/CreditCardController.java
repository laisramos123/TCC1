package com.example.resource_server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.example.resource_server.model.CreditCard;
import com.example.resource_server.service.CreditCardService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/open-banking/credit-cards/v2")
public class CreditCardController {

    @Autowired
    private CreditCardService creditCardService;

    @GetMapping("/accounts")
    @PreAuthorize("hasAuthority('SCOPE_credit-cards-accounts')")
    public ResponseEntity<Map<String, Object>> getCreditCards(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("x-fapi-interaction-id") String interactionId) {

        String cpf = jwt.getClaimAsString("cpf");
        List<CreditCard> cards = creditCardService.getCreditCards(cpf);

        Map<String, Object> response = Map.of(
                "data", cards,
                "links", Map.of("self", "/open-banking/credit-cards/v2/accounts"),
                "meta", Map.of("totalRecords", cards.size()));

        return ResponseEntity
                .ok()
                .header("x-fapi-interaction-id", interactionId)
                .body(response);
    }

    @GetMapping("/accounts/{cardId}")
    @PreAuthorize("hasAuthority('SCOPE_credit-cards-accounts')")
    public ResponseEntity<Map<String, Object>> getCreditCardDetails(
            @PathVariable String cardId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("x-fapi-interaction-id") String interactionId) {

        String cpf = jwt.getClaimAsString("cpf");
        Map<String, Object> cardDetails = creditCardService.getCreditCardDetails(cardId, cpf);

        return ResponseEntity
                .ok()
                .header("x-fapi-interaction-id", interactionId)
                .body(cardDetails);
    }
}