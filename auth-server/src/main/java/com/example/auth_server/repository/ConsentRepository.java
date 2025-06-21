package com.example.auth_server.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.auth_server.model.Consent;

public interface ConsentRepository extends JpaRepository<Consent, Long> {
        Consent findByConsentId(String consentId);

        /**
         * Busca o ultimo consentimento com status especifico para um usuario e cliente
         * Ordenado por data de criacão (mais recente primeiro)
         */
        Consent findTopByUserIdAndClientIdAndStatusOrderByCreatedAtDesc(
                        String userId, String clientId, String status);

        /**
         * Busca consentimentos por usuario, cliente e lista de status
         */
        List<Consent> findByUserIdAndClientIdAndStatusIn(
                        String userId, String clientId, List<String> statuses);

        /**
         * Busca todos os consentimentos de um usuario ordenados por data de criacão
         */
        List<Consent> findByUserIdOrderByCreatedAtDesc(String userId);

        /**
         * Busca consentimentos ativos (não revogados) de um usuario
         */
        List<Consent> findByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(String userId);

        /**
         * Busca todos os consentimentos de um cliente (TPP) ordenados por data
         */
        List<Consent> findByClientIdOrderByCreatedAtDesc(String clientId);

        /**
         * Busca consentimentos de um usuario para um cliente especifico
         */
        List<Consent> findByUserIdAndClientIdOrderByCreatedAtDesc(String userId, String clientId);

        /**
         * Conta quantos consentimentos com status especifico um usuario tem para um
         * cliente
         */
        long countByUserIdAndClientIdAndStatus(String userId, String clientId, String status);

        /**
         * Conta consentimentos por status
         */
        long countByStatus(String status);

        /**
         * Busca consentimentos expirados que não foram revogados
         */
        List<Consent> findByExpiresAtBeforeAndStatusNot(LocalDateTime expiresAt, String status);

        /**
         * Busca consentimentos que expiram em X dias
         */
        @Query("SELECT c FROM Consent c WHERE c.expiresAt BETWEEN :startDate AND :endDate AND c.status = :status")
        List<Consent> findConsentsExpiringBetween(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("status") String status);

        /**
         * Busca consentimentos autorizados e validos para um cliente
         */
        @Query("SELECT c FROM Consent c WHERE c.clientId = :clientId AND c.status = :status AND " +
                        "(c.expiresAt IS NULL OR c.expiresAt > :now) AND c.revokedAt IS NULL")
        List<Consent> findActiveConsentsByClient(
                        @Param("clientId") String clientId,
                        @Param("status") String status,
                        @Param("now") LocalDateTime now);

        /**
         * Busca consentimentos de um usuario que incluem uma permissão especifica
         */
        @Query("SELECT c FROM Consent c JOIN c.permissions p WHERE c.userId = :userId AND p = :permission")
        List<Consent> findByUserIdAndPermission(@Param("userId") String userId, @Param("permission") String permission);

        /**
         * Busca consentimentos criados entre duas datas
         */
        List<Consent> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

        /**
         * Busca consentimentos por status criados após uma data
         */
        List<Consent> findByStatusAndCreatedAtAfter(String status, LocalDateTime createdAfter);

        /**
         * Verifica se existe consentimento ativo para usuario/cliente/permissão
         */
        @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Consent c " +
                        "JOIN c.permissions p WHERE c.userId = :userId AND c.clientId = :clientId AND " +
                        "p = :permission AND c.status = 'AUTHORIZED' AND " +
                        "(c.expiresAt IS NULL OR c.expiresAt > :now) AND c.revokedAt IS NULL")
        boolean existsActiveConsentForPermission(
                        @Param("userId") String userId,
                        @Param("clientId") String clientId,
                        @Param("permission") String permission,
                        @Param("now") LocalDateTime now);

        /**
         * Remove consentimentos antigos (para limpeza de dados)
         */
        @Modifying
        @Query("DELETE FROM Consent c WHERE c.createdAt < :createdBefore AND c.status IN :statuses")
        void deleteByCreatedAtBeforeAndStatusIn(
                        @Param("createdBefore") LocalDateTime createdBefore,
                        @Param("statuses") List<String> statuses);

        /**
         * Estatisticas de consentimentos por cliente
         */
        @Query("SELECT c.clientId, COUNT(c) FROM Consent c GROUP BY c.clientId")
        List<Object[]> getConsentCountByClient();

        /**
         * Estatisticas de consentimentos por status
         */
        @Query("SELECT c.status, COUNT(c) FROM Consent c GROUP BY c.status")
        List<Object[]> getConsentCountByStatus();

        /**
         * Busca consentimentos que precisam ser renovados (próximos ao vencimento)
         */
        @Query("SELECT c FROM Consent c WHERE c.status = 'AUTHORIZED' AND " +
                        "c.expiresAt BETWEEN :now AND :renewalThreshold AND c.revokedAt IS NULL")
        List<Consent> findConsentsNeedingRenewal(
                        @Param("now") LocalDateTime now,
                        @Param("renewalThreshold") LocalDateTime renewalThreshold);
}