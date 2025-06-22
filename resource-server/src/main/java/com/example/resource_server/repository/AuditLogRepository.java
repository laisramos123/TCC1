package com.example.resource_server.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.resource_server.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserIdAndTimestampBetween(
            String userId, LocalDateTime startTime, LocalDateTime endTime);

    List<AuditLog> findByConsentId(String consentId);
}
