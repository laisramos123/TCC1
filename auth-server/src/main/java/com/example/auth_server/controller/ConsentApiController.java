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

/**
 * FASE 1: CONSENT API
 * Gerencia o ciclo de vida dos consentimentos
 */
@RestController
@RequestMapping("/open-banking/consents/v2/consents")
public class ConsentApiController {

        @Autowired
        private ConsentService consentService;

        /**
         * Criar consentimento
         * POST /open-banking/consents/v2/consents
         * 
         * @throws BadRequestException
         */
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

        /**
         * Consultar consentimento
         * GET /open-banking/consents/v2/consents/{consentId}
         */
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

        /**
         * Deletar/Revogar consentimento
         * DELETE /open-banking/consents/v2/consents/{consentId}
         */
        @DeleteMapping("/{consentId}")
        public ResponseEntity<Void> deleteConsent(
                        @PathVariable String consentId,
                        @RequestHeader("x-fapi-interaction-id") String interactionId,
                        @RequestHeader(value = "x-revoked-by", defaultValue = "TPP") String revokedBy) {

                consentService.revokeConsent(consentId, "TPP_REQUESTED", revokedBy);

                return ResponseEntity
                                .status(HttpStatus.NO_CONTENT)
                                .header("x-fapi-interaction-id", interactionId)
                                .build();
        }

        /**
         * Listar consentimentos (opcional)
         * GET /open-banking/consents/v2/consents?cpf=12345678900
         */
        @GetMapping
        public ResponseEntity<ConsentListResponse> listConsents(
                        @RequestParam(required = false) String cpf,
                        @RequestHeader("x-fapi-interaction-id") String interactionId) {

                List<ConsentResponse> consents = consentService.listConsents(cpf);

                ConsentListResponse response = ConsentListResponse.builder()
                                .data(consents.stream()
                                                .map(ConsentResponse::getData)
                                                .toList())
                                .meta(ConsentResponse.Meta.builder()
                                                .totalRecords(consents.size())
                                                .totalPages(1)
                                                .build())
                                .build();

                return ResponseEntity
                                .ok()
                                .header("x-fapi-interaction-id", interactionId)
                                .body(response);
        }
}