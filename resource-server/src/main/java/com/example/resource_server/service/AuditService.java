package com.example.resource_server.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.resource_server.model.AuditLog;
import com.example.resource_server.repository.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Registra uma ação de auditoria
     * Usa PROPAGATION_REQUIRES_NEW para garantir que o log seja salvo mesmo se a
     * transação principal falhar
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logAction(Jwt jwt, String resourceType, String resourceId, String action, String status,
            String failureReason) {
        AuditLog log = new AuditLog();
        log.setUserId(jwt.getSubject());
        log.setClientId(jwt.getClaimAsString("client_id"));
        log.setConsentId(jwt.getClaimAsString("consent_id"));
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());
        log.setIpAddress(getClientIpAddress());
        log.setStatus(status);
        log.setFailureReason(failureReason);

        return auditLogRepository.save(log);
    }

    /**
     * Obtém logs de auditoria de um usuário em um período
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getUserLogs(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByUserIdAndTimestampBetween(userId, startTime, endTime);
    }

    /**
     * Obtém logs de auditoria de um consentimento específico
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getConsentLogs(String consentId) {
        return auditLogRepository.findByConsentId(consentId);
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String forwardedHeader = request.getHeader("X-Forwarded-For");

                if (forwardedHeader != null && !forwardedHeader.isEmpty()) {
                    // O primeiro IP na lista X-Forwarded-For é o IP do cliente
                    return forwardedHeader.split(",")[0].trim();
                }

                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Ignora exceções e retorna endereço desconhecido
        }

        return "unknown";
    }
}
