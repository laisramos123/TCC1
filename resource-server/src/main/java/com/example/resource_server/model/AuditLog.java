package com.example.resource_server.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String clientId;

    private String consentId;

    private String resourceType;

    private String resourceId;

    private String action;

    private LocalDateTime timestamp;

    private String ipAddress;

    private String status; // SUCCESS, FAILED

    private String failureReason;
}