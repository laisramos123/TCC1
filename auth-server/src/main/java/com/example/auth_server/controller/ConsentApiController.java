package com.example.auth_server.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.auth_server.dto.ConsentListResponse;
import com.example.auth_server.dto.ConsentRequest;
import com.example.auth_server.dto.ConsentResponse;
import com.example.auth_server.service.ConsentService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/open-banking/consents/v2/consents")
public class ConsentApiController {

    @Autowired
    private ConsentService consentService;

    @PostMapping
    public ResponseEntity<ConsentResponse> createConsent(
            @RequestBody ConsentRequest request,
            @RequestHeader("x-fapi-interaction-id") String interactionId) throws BadRequestException {

        try {
            ConsentResponse response = consentService.createConsent(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header("x-fapi-interaction-id", interactionId)
                    .body(response);

        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @GetMapping("/{consentId}")
    public ResponseEntity<ConsentResponse> getConsent(
            @PathVariable String consentId,
            @RequestHeader("x-fapi-interaction-id") String interactionId) {

        ConsentResponse response = consentService.getConsent(consentId);

        return ResponseEntity
                .ok()
                .header("x-fapi-interaction-id", interactionId)
                .body(response);
    }

    @DeleteMapping("/{consentId}")
    public ResponseEntity<Void> deleteConsent(
            @PathVariable String consentId,
            @RequestHeader("x-fapi-interaction-id") String interactionId,
            @RequestHeader(value = "x-revoked-by", defaultValue = "TPP") String revokedBy) {

        // Chamada correta com 3 parâmetros
        consentService.revokeConsent(consentId, "TPP_REQUESTED", revokedBy);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .header("x-fapi-interaction-id", interactionId)
                .build();
    }

    @GetMapping
    public ResponseEntity<ConsentListResponse> listConsents(
            @RequestParam(required = false) String cpf,
            @RequestHeader("x-fapi-interaction-id") String interactionId) {

        List<ConsentResponse> consents = consentService.listConsents(cpf);

        ConsentListResponse response = new ConsentListResponse();
        response.setData(consents.stream()
                .map(ConsentResponse::getData)
                .collect(Collectors.toList()));
        
        ConsentListResponse.Meta meta = new ConsentListResponse.Meta();
        meta.setTotalRecords(consents.size());
        meta.setTotalPages(1);
        response.setMeta(meta);

        return ResponseEntity
                .ok()
                .header("x-fapi-interaction-id", interactionId)
                .body(response);
    }
}
