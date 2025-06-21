package com.example.auth_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_server.model.Consent;
import com.example.auth_server.repository.ConsentRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class ConsentService {

    // Constantes para os status
    public static final String STATUS_AWAITING_AUTHORIZATION = "AWAITING_AUTHORIZATION";
    public static final String STATUS_AUTHORIZED = "AUTHORIZED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_REVOKED = "REVOKED";

    // Periodo padrão de expiracão (12 meses conforme Open Finance Brasil)
    private static final int DEFAULT_EXPIRATION_MONTHS = 12;

    @Autowired
    private ConsentRepository consentRepository;

    /**
     * Cria um novo consentimento
     */
    public Consent createConsent(String userId, String clientId, Set<String> permissions) {
        System.out.println("🎯 Criando novo consentimento para usuario: " + userId + ", cliente: " + clientId);

        // Verificar se ja existe consentimento pendente para este
        // usuario/cliente/permissões
        Consent existingConsent = findValidConsent(userId, clientId, permissions);
        if (existingConsent != null && STATUS_AWAITING_AUTHORIZATION.equals(existingConsent.getStatus())) {
            System.out.println("♻️ Reutilizando consentimento existente: " + existingConsent.getConsentId());
            return existingConsent;
        }

        Consent consent = new Consent();
        consent.setConsentId(generateConsentId());
        consent.setUserId(userId);
        consent.setClientId(clientId); // ✅ Mudança: agora usa setClientId()
        consent.setPermissions(permissions);
        consent.setStatus(STATUS_AWAITING_AUTHORIZATION);
        consent.setCreatedAt(LocalDateTime.now());
        consent.setExpiresAt(LocalDateTime.now().plus(DEFAULT_EXPIRATION_MONTHS, ChronoUnit.MONTHS));

        Consent savedConsent = consentRepository.save(consent);

        System.out.println(" Consentimento criado com sucesso:");
        System.out.println("   - ID: " + savedConsent.getConsentId());
        System.out.println("   - Status: " + savedConsent.getStatus());
        System.out.println("   - Cliente: " + savedConsent.getClientId()); // ✅ Mudança: getClientId()
        System.out.println("   - Expira em: " + savedConsent.getExpiresAt());
        System.out.println("   - Permissões: " + savedConsent.getPermissions());

        return savedConsent;
    }

    /**
     * Busca consentimento por consentId
     */
    public Consent findByConsentId(String consentId) {
        System.out.println(" Buscando consentimento por ID: " + consentId);
        return consentRepository.findByConsentId(consentId);
    }

    /**
     * Busca o último consentimento AWAITING_AUTHORIZATION para um usuario e cliente
     * especificos
     */
    public Consent findLatestPendingConsent(String userId, String clientId) {
        System.out.println(
                " Buscando ultimo consentimento pendente para usuario: " + userId + ", cliente: " + clientId);
        return consentRepository.findTopByUserIdAndClientIdAndStatusOrderByCreatedAtDesc(
                userId, clientId, STATUS_AWAITING_AUTHORIZATION);
    }

    /**
     * Busca consentimento valido existente (AWAITING_AUTHORIZATION ou AUTHORIZED)
     */
    public Consent findValidConsent(String userId, String clientId, Set<String> permissions) {
        System.out.println(" Buscando consentimento valido para usuario: " + userId + ", cliente: " + clientId);

        List<String> validStatuses = List.of(STATUS_AWAITING_AUTHORIZATION, STATUS_AUTHORIZED);
        List<Consent> consents = consentRepository.findByUserIdAndClientIdAndStatusIn(
                userId, clientId, validStatuses);

        // Verificar se algum consentimento tem as permissões necessarias e não expirou
        for (Consent consent : consents) {
            if (isConsentValid(consent) && consent.getPermissions().containsAll(permissions)) {
                System.out.println("Consentimento valido encontrado: " + consent.getConsentId());
                return consent;
            }
        }

        System.out.println(" Nenhum consentimento valido encontrado");
        return null;
    }

    /**
     * Aprova um consentimento
     */
    public void approveConsent(String consentId) {
        System.out.println(" Aprovando consentimento: " + consentId);

        Consent consent = consentRepository.findByConsentId(consentId);
        if (consent == null) {
            throw new RuntimeException("Consentimento não encontrado: " + consentId);
        }

        if (!STATUS_AWAITING_AUTHORIZATION.equals(consent.getStatus())) {
            throw new RuntimeException("Consentimento não esta pendente de aprovacão: " + consentId +
                    " (Status atual: " + consent.getStatus() + ")");
        }

        if (!isConsentValid(consent)) {
            throw new RuntimeException("Consentimento expirado: " + consentId);
        }

        consent.setStatus(STATUS_AUTHORIZED);
        consentRepository.save(consent);

        System.out.println(" Consentimento aprovado com sucesso: " + consentId);
    }

    /**
     * Nega um consentimento
     */
    public void denyConsent(String consentId) {
        System.out.println(" Negando consentimento: " + consentId);

        Consent consent = consentRepository.findByConsentId(consentId);
        if (consent == null) {
            throw new RuntimeException("Consentimento não encontrado: " + consentId);
        }

        if (!STATUS_AWAITING_AUTHORIZATION.equals(consent.getStatus())) {
            throw new RuntimeException("Consentimento não esta pendente de aprovacão: " + consentId +
                    " (Status atual: " + consent.getStatus() + ")");
        }

        consent.setStatus(STATUS_REJECTED);
        consent.setRevokedAt(LocalDateTime.now());
        consentRepository.save(consent);

        System.out.println(" Consentimento negado: " + consentId);
    }

    /**
     * Revoga um consentimento autorizado
     */
    public void revokeConsent(String consentId) {
        System.out.println(" Revogando consentimento: " + consentId);

        Consent consent = consentRepository.findByConsentId(consentId);
        if (consent == null) {
            throw new RuntimeException("Consentimento não encontrado: " + consentId);
        }

        if (!STATUS_AUTHORIZED.equals(consent.getStatus())) {
            throw new RuntimeException("Apenas consentimentos autorizados podem ser revogados: " + consentId +
                    " (Status atual: " + consent.getStatus() + ")");
        }

        consent.setStatus(STATUS_REVOKED);
        consent.setRevokedAt(LocalDateTime.now());
        consentRepository.save(consent);

        System.out.println(" Consentimento revogado: " + consentId);
    }

    /**
     * Lista todos os consentimentos de um usuario
     */
    public List<Consent> findUserConsents(String userId) {
        System.out.println(" Listando consentimentos do usuario: " + userId);
        return consentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Lista consentimentos ativos (não revogados) de um usuario
     */
    public List<Consent> findActiveUserConsents(String userId) {
        System.out.println(" Listando consentimentos ativos do usuario: " + userId);
        return consentRepository.findByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(userId);
    }

    /**
     * Lista todos os consentimentos de um cliente (TPP)
     */
    public List<Consent> findClientConsents(String clientId) {
        System.out.println(" Listando consentimentos do cliente: " + clientId);
        return consentRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    /**
     * Conta quantos consentimentos pendentes um usuario tem para um cliente
     */
    public long countPendingConsents(String userId, String clientId) {
        return consentRepository.countByUserIdAndClientIdAndStatus(userId, clientId, STATUS_AWAITING_AUTHORIZATION);
    }

    /**
     * Verifica se um consentimento ainda é valido (não expirou)
     */
    public boolean isConsentValid(Consent consent) {
        if (consent == null) {
            return false;
        }

        // Verifica se foi revogado
        if (consent.getRevokedAt() != null) {
            return false;
        }

        // Verifica se esta rejeitado ou revogado
        if (STATUS_REJECTED.equals(consent.getStatus()) || STATUS_REVOKED.equals(consent.getStatus())) {
            return false;
        }

        // Verifica se expirou
        if (consent.getExpiresAt() != null && LocalDateTime.now().isAfter(consent.getExpiresAt())) {
            return false;
        }

        return true;
    }

    /**
     * Verifica se um consentimento esta autorizado e valido
     */
    public boolean isConsentAuthorizedAndValid(String consentId) {
        Consent consent = findByConsentId(consentId);
        return consent != null &&
                STATUS_AUTHORIZED.equals(consent.getStatus()) &&
                isConsentValid(consent);
    }

    /**
     * Limpa consentimentos expirados automaticamente
     */
    @Transactional
    public void cleanupExpiredConsents() {
        System.out.println(" Limpando consentimentos expirados...");

        LocalDateTime now = LocalDateTime.now();
        List<Consent> expiredConsents = consentRepository.findByExpiresAtBeforeAndStatusNot(now, STATUS_REVOKED);

        for (Consent consent : expiredConsents) {
            if (!STATUS_REVOKED.equals(consent.getStatus())) {
                consent.setStatus(STATUS_REVOKED);
                consent.setRevokedAt(now);
                consentRepository.save(consent);
                System.out.println("🗑️ Consentimento expirado revogado: " + consent.getConsentId());
            }
        }

        System.out.println(" Limpeza concluida. " + expiredConsents.size() + " consentimentos processados.");
    }

    /**
     * Gera um ID único para o consentimento
     */
    private String generateConsentId() {
        return "consent_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Valida permissões de acordo com Open Finance Brasil
     */
    public boolean validatePermissions(Set<String> permissions) {
        // Permissões validas do Open Finance Brasil
        Set<String> validPermissions = Set.of(
                "openid", "profile", "email",
                "accounts", "credit-cards-accounts",
                "loans", "financings", "invoice-financings",
                "unarranged-accounts-overdraft");

        return validPermissions.containsAll(permissions);
    }

    /**
     * Estatisticas de consentimentos
     */
    public ConsentStats getConsentStats() {
        long total = consentRepository.count();
        long authorized = consentRepository.countByStatus(STATUS_AUTHORIZED);
        long pending = consentRepository.countByStatus(STATUS_AWAITING_AUTHORIZATION);
        long rejected = consentRepository.countByStatus(STATUS_REJECTED);
        long revoked = consentRepository.countByStatus(STATUS_REVOKED);

        return new ConsentStats(total, authorized, pending, rejected, revoked);
    }

    // Classe interna para estatisticas
    public static class ConsentStats {
        private final long total;
        private final long authorized;
        private final long pending;
        private final long rejected;
        private final long revoked;

        public ConsentStats(long total, long authorized, long pending, long rejected, long revoked) {
            this.total = total;
            this.authorized = authorized;
            this.pending = pending;
            this.rejected = rejected;
            this.revoked = revoked;
        }

        // Getters
        public long getTotal() {
            return total;
        }

        public long getAuthorized() {
            return authorized;
        }

        public long getPending() {
            return pending;
        }

        public long getRejected() {
            return rejected;
        }

        public long getRevoked() {
            return revoked;
        }

        @Override
        public String toString() {
            return String.format("ConsentStats{total=%d, authorized=%d, pending=%d, rejected=%d, revoked=%d}",
                    total, authorized, pending, rejected, revoked);
        }
    }
}