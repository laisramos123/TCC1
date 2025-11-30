package com.example.auth_server.controller;

import com.example.auth_server.dto.ConsentRequest;
import com.example.auth_server.dto.ConsentResponse;
import com.example.auth_server.enums.ConsentStatus;
import com.example.auth_server.service.ConsentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller REST para a API Open Banking de Consents
 * Endpoint base: /open-banking/consents/v2/consents
 */
@RestController
@RequestMapping("/open-banking/consents/v2/consents")
public class ConsentController {

    private static final Logger logger = LoggerFactory.getLogger(ConsentController.class);

    @Autowired
    private ConsentService consentService;

    /**
     * POST /open-banking/consents/v2/consents
     * Cria um novo consentimento
     */
    @PostMapping
    public ResponseEntity<ConsentResponse> createConsent(
            @RequestBody ConsentRequest request,
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String interactionId) {

        logger.info("========================================");
        logger.info("📝 POST /open-banking/consents/v2/consents");
        logger.info("   Interaction ID: {}", interactionId);

        if (request.getData() != null && request.getData().getLoggedUser() != null
                && request.getData().getLoggedUser().getDocument() != null) {
            logger.info("   CPF: {}", request.getData().getLoggedUser().getDocument().getIdentification());
            logger.info("   Permissions: {}", request.getData().getPermissions());
        }
        logger.info("========================================");

        try {
            ConsentResponse response = consentService.createConsent(request);
            logger.info("✅ Consent criado: {}", response.getData().getConsentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("❌ Erro ao criar consent: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao criar consent: " + e.getMessage());
        }
    }

    /**
     * GET /open-banking/consents/v2/consents/{consentId}
     * Consulta um consentimento
     */
    @GetMapping("/{consentId}")
    public ResponseEntity<ConsentResponse> getConsent(
            @PathVariable String consentId,
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String interactionId) {

        logger.info("🔍 GET /open-banking/consents/v2/consents/{}", consentId);

        try {
            ConsentResponse response = consentService.getConsent(consentId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Consent não encontrado: {}", consentId);
            throw new RuntimeException("Consent não encontrado: " + consentId);
        }
    }

    /**
     * DELETE /open-banking/consents/v2/consents/{consentId}
     * Revoga um consentimento
     */
    @DeleteMapping("/{consentId}")
    public ResponseEntity<Void> revokeConsent(
            @PathVariable String consentId,
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String interactionId) {

        logger.info("🗑️ DELETE /open-banking/consents/v2/consents/{}", consentId);

        try {
            consentService.revokeConsent(consentId, "USER_REQUEST", "user");
            logger.info("✅ Consent revogado: {}", consentId);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("❌ Erro ao revogar consent: {}", e.getMessage());
            throw new RuntimeException("Erro ao revogar consent: " + e.getMessage());
        }
    }

    /**
     * PATCH /open-banking/consents/v2/consents/{consentId}/consumed
     * Marca consent como consumido
     */
    @PatchMapping("/{consentId}/consumed")
    public ResponseEntity<Void> markAsConsumed(
            @PathVariable String consentId,
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String interactionId) {

        logger.info("📦 PATCH /open-banking/consents/v2/consents/{}/consumed", consentId);

        try {
            consentService.updateStatus(consentId, ConsentStatus.CONSUMED);
            logger.info("✅ Consent marcado como consumido: {}", consentId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("❌ Erro ao marcar consent como consumido: {}", e.getMessage());
            throw new RuntimeException("Erro ao marcar consent como consumido: " + e.getMessage());
        }
    }

    /**
     * PATCH /open-banking/consents/v2/consents/{consentId}/authorise
     * Autoriza um consent (muda status para AUTHORISED)
     */
    @PatchMapping("/{consentId}/authorise")
    public ResponseEntity<ConsentResponse> authoriseConsent(
            @PathVariable String consentId,
            @RequestHeader(value = "x-fapi-interaction-id", required = false) String interactionId) {

        logger.info("✅ PATCH /open-banking/consents/v2/consents/{}/authorise", consentId);

        try {
            consentService.updateStatus(consentId, ConsentStatus.AUTHORISED);
            ConsentResponse response = consentService.getConsent(consentId);
            logger.info("✅ Consent autorizado: {}", consentId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Erro ao autorizar consent: {}", e.getMessage());
            throw new RuntimeException("Erro ao autorizar consent: " + e.getMessage());
        }
    }
}