package com.example.auth_server.controller;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.auth_server.model.Consent;
import com.example.auth_server.service.ConsentService;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/consents")
@Validated
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    /**
     * Cria um novo consentimento
     * POST /api/consents
     */
    @PostMapping
    public ResponseEntity<ConsentResponse> createConsent(@Valid @RequestBody CreateConsentRequest request) {
        try {
            System.out.println("📝 API: Criando novo consentimento via REST API");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            System.out.println("  - Usuário: " + userId);
            System.out.println("  - Cliente: " + request.getClientId());
            System.out.println("  - Permissões: " + request.getPermissions());

            // Validar permissões
            if (!consentService.validatePermissions(request.getPermissions())) {
                return ResponseEntity.badRequest()
                        .body(new ConsentResponse(null, "Permissões inválidas", request.getPermissions()));
            }

            // Criar consentimento
            Consent consent = consentService.createConsent(userId, request.getClientId(), request.getPermissions());

            ConsentResponse response = new ConsentResponse(consent, "Consentimento criado com sucesso", null);

            System.out.println("✅ API: Consentimento criado - ID: " + consent.getConsentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ API: Erro ao criar consentimento: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ConsentResponse(null, "Erro ao criar consentimento: " + e.getMessage(), null));
        }
    }

    /**
     * Lista todos os consentimentos do usuário logado
     * GET /api/consents
     */
    @GetMapping
    public ResponseEntity<List<ConsentSummary>> getUserConsents(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "clientId", required = false) String clientId) {

        try {
            System.out.println("📋 API: Listando consentimentos do usuário");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            List<Consent> consents;

            if (status != null && "active".equals(status)) {
                // Buscar apenas consentimentos ativos
                consents = consentService.findActiveUserConsents(userId);
            } else {
                // Buscar todos os consentimentos
                consents = consentService.findUserConsents(userId);
            }

            // Filtrar por cliente se especificado
            if (clientId != null && !clientId.trim().isEmpty()) {
                consents = consents.stream()
                        .filter(c -> clientId.equals(c.getClientId()))
                        .toList();
            }

            // Converter para resumo (sem informações sensíveis)
            List<ConsentSummary> summaries = consents.stream()
                    .map(this::toConsentSummary)
                    .toList();

            System.out.println("✅ API: Retornando " + summaries.size() + " consentimentos");
            return ResponseEntity.ok(summaries);

        } catch (Exception e) {
            System.err.println("❌ API: Erro ao listar consentimentos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    /**
     * Busca um consentimento específico por ID
     * GET /api/consents/{consentId}
     */
    @GetMapping("/{consentId}")
    public ResponseEntity<ConsentDetailResponse> getConsent(@PathVariable @NotBlank String consentId) {
        try {
            System.out.println("🔍 API: Buscando consentimento: " + consentId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            Consent consent = consentService.findByConsentId(consentId);

            if (consent == null) {
                System.out.println("❌ API: Consentimento não encontrado: " + consentId);
                return ResponseEntity.notFound().build();
            }

            // Verificar se o consentimento pertence ao usuário logado
            if (!consent.getUserId().equals(userId)) {
                System.out.println("❌ API: Acesso negado ao consentimento: " + consentId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ConsentDetailResponse response = toConsentDetailResponse(consent);

            System.out.println("✅ API: Consentimento encontrado: " + consentId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ API: Erro ao buscar consentimento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Autoriza/Aprova um consentimento
     * PUT /api/consents/{consentId}/authorize
     */
    @PutMapping("/{consentId}/authorize")
    public ResponseEntity<ConsentResponse> authorizeConsent(@PathVariable @NotBlank String consentId) {
        try {
            System.out.println("✅ API: Autorizando consentimento: " + consentId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            // Verificar se o consentimento existe e pertence ao usuário
            Consent consent = consentService.findByConsentId(consentId);
            if (consent == null) {
                return ResponseEntity.notFound().build();
            }

            if (!consent.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Aprovar o consentimento
            consentService.approveConsent(consentId);

            // Buscar o consentimento atualizado
            Consent updatedConsent = consentService.findByConsentId(consentId);

            ConsentResponse response = new ConsentResponse(updatedConsent, "Consentimento autorizado com sucesso",
                    null);

            System.out.println("✅ API: Consentimento autorizado: " + consentId);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("❌ API: Erro ao autorizar consentimento: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ConsentResponse(null, "Erro ao autorizar: " + e.getMessage(), null));
        } catch (Exception e) {
            System.err.println("❌ API: Erro interno ao autorizar consentimento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Rejeita um consentimento
     * PUT /api/consents/{consentId}/reject
     */
    @PutMapping("/{consentId}/reject")
    public ResponseEntity<ConsentResponse> rejectConsent(@PathVariable @NotBlank String consentId) {
        try {
            System.out.println("❌ API: Rejeitando consentimento: " + consentId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            // Verificar se o consentimento existe e pertence ao usuário
            Consent consent = consentService.findByConsentId(consentId);
            if (consent == null) {
                return ResponseEntity.notFound().build();
            }

            if (!consent.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Negar o consentimento
            consentService.denyConsent(consentId);

            // Buscar o consentimento atualizado
            Consent updatedConsent = consentService.findByConsentId(consentId);

            ConsentResponse response = new ConsentResponse(updatedConsent, "Consentimento rejeitado com sucesso", null);

            System.out.println("❌ API: Consentimento rejeitado: " + consentId);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("❌ API: Erro ao rejeitar consentimento: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ConsentResponse(null, "Erro ao rejeitar: " + e.getMessage(), null));
        } catch (Exception e) {
            System.err.println("❌ API: Erro interno ao rejeitar consentimento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Revoga um consentimento
     * PUT /api/consents/{consentId}/revoke
     */
    @PutMapping("/{consentId}/revoke")
    public ResponseEntity<ConsentResponse> revokeConsent(@PathVariable @NotBlank String consentId) {
        try {
            System.out.println("🔄 API: Revogando consentimento: " + consentId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            // Verificar se o consentimento existe e pertence ao usuário
            Consent consent = consentService.findByConsentId(consentId);
            if (consent == null) {
                return ResponseEntity.notFound().build();
            }

            if (!consent.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Revogar o consentimento
            consentService.revokeConsent(consentId);

            // Buscar o consentimento atualizado
            Consent updatedConsent = consentService.findByConsentId(consentId);

            ConsentResponse response = new ConsentResponse(updatedConsent, "Consentimento revogado com sucesso", null);

            System.out.println("✅ API: Consentimento revogado: " + consentId);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("❌ API: Erro ao revogar consentimento: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ConsentResponse(null, "Erro ao revogar: " + e.getMessage(), null));
        } catch (Exception e) {
            System.err.println("❌ API: Erro interno ao revogar consentimento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verifica status de um consentimento
     * GET /api/consents/{consentId}/status
     */
    @GetMapping("/{consentId}/status")
    public ResponseEntity<ConsentStatusResponse> getConsentStatus(@PathVariable @NotBlank String consentId) {
        try {
            System.out.println("📊 API: Verificando status do consentimento: " + consentId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            Consent consent = consentService.findByConsentId(consentId);

            if (consent == null) {
                return ResponseEntity.notFound().build();
            }

            if (!consent.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean isValid = consentService.isConsentValid(consent);
            boolean isAuthorized = ConsentService.STATUS_AUTHORIZED.equals(consent.getStatus());

            ConsentStatusResponse response = new ConsentStatusResponse(
                    consentId,
                    consent.getStatus(),
                    isValid,
                    isAuthorized,
                    consent.getCreatedAt(),
                    consent.getExpiresAt(),
                    consent.getRevokedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ API: Erro ao verificar status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===========================================
    // MÉTODOS AUXILIARES PARA CONVERSÃO DE DADOS
    // ===========================================

    private ConsentSummary toConsentSummary(Consent consent) {
        return new ConsentSummary(
                consent.getConsentId(),
                consent.getClientId(),
                consent.getStatus(),
                consent.getCreatedAt(),
                consent.getExpiresAt(),
                consent.getPermissions().size(),
                consentService.isConsentValid(consent));
    }

    private ConsentDetailResponse toConsentDetailResponse(Consent consent) {
        return new ConsentDetailResponse(
                consent.getConsentId(),
                consent.getUserId(),
                consent.getClientId(),
                consent.getStatus(),
                consent.getCreatedAt(),
                consent.getExpiresAt(),
                consent.getRevokedAt(),
                consent.getPermissions(),
                consentService.isConsentValid(consent));
    }

    // ===========================================
    // CLASSES DTO PARA REQUESTS E RESPONSES
    // ===========================================

    // Request para criar consentimento
    public static class CreateConsentRequest {
        @NotBlank(message = "Client ID é obrigatório")
        private String clientId;

        @NotEmpty(message = "Pelo menos uma permissão é obrigatória")
        private Set<String> permissions;

        // Constructors
        public CreateConsentRequest() {
        }

        public CreateConsentRequest(String clientId, Set<String> permissions) {
            this.clientId = clientId;
            this.permissions = permissions;
        }

        // Getters e Setters
        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(Set<String> permissions) {
            this.permissions = permissions;
        }
    }

    // Response geral
    public static class ConsentResponse {
        private Consent consent;
        private String message;
        private Set<String> invalidPermissions;

        public ConsentResponse(Consent consent, String message, Set<String> invalidPermissions) {
            this.consent = consent;
            this.message = message;
            this.invalidPermissions = invalidPermissions;
        }

        // Getters
        public Consent getConsent() {
            return consent;
        }

        public String getMessage() {
            return message;
        }

        public Set<String> getInvalidPermissions() {
            return invalidPermissions;
        }
    }

    // Resumo de consentimento (para listagem)
    public static class ConsentSummary {
        private String consentId;
        private String clientId;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private int permissionCount;
        private boolean valid;

        public ConsentSummary(String consentId, String clientId, String status,
                LocalDateTime createdAt, LocalDateTime expiresAt,
                int permissionCount, boolean valid) {
            this.consentId = consentId;
            this.clientId = clientId;
            this.status = status;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.permissionCount = permissionCount;
            this.valid = valid;
        }

        // Getters
        public String getConsentId() {
            return consentId;
        }

        public String getClientId() {
            return clientId;
        }

        public String getStatus() {
            return status;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }

        public int getPermissionCount() {
            return permissionCount;
        }

        public boolean isValid() {
            return valid;
        }
    }

    // Detalhes completos do consentimento
    public static class ConsentDetailResponse {
        private String consentId;
        private String userId;
        private String clientId;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private LocalDateTime revokedAt;
        private Set<String> permissions;
        private boolean valid;

        public ConsentDetailResponse(String consentId, String userId, String clientId, String status,
                LocalDateTime createdAt, LocalDateTime expiresAt, LocalDateTime revokedAt,
                Set<String> permissions, boolean valid) {
            this.consentId = consentId;
            this.userId = userId;
            this.clientId = clientId;
            this.status = status;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.revokedAt = revokedAt;
            this.permissions = permissions;
            this.valid = valid;
        }

        // Getters
        public String getConsentId() {
            return consentId;
        }

        public String getUserId() {
            return userId;
        }

        public String getClientId() {
            return clientId;
        }

        public String getStatus() {
            return status;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }

        public LocalDateTime getRevokedAt() {
            return revokedAt;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public boolean isValid() {
            return valid;
        }
    }

    // Response para status
    public static class ConsentStatusResponse {
        private String consentId;
        private String status;
        private boolean valid;
        private boolean authorized;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private LocalDateTime revokedAt;

        public ConsentStatusResponse(String consentId, String status, boolean valid, boolean authorized,
                LocalDateTime createdAt, LocalDateTime expiresAt, LocalDateTime revokedAt) {
            this.consentId = consentId;
            this.status = status;
            this.valid = valid;
            this.authorized = authorized;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.revokedAt = revokedAt;
        }

        // Getters
        public String getConsentId() {
            return consentId;
        }

        public String getStatus() {
            return status;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isAuthorized() {
            return authorized;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }

        public LocalDateTime getRevokedAt() {
            return revokedAt;
        }
    }
}