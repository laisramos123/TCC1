package com.example.resource_server.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "consent_access_logs")
@Getter
@Setter
public class ConsentAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", nullable = false)
    private ConsentRecord consent;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "resource_type", nullable = false)
    private String resourceType; // ACCOUNTS, TRANSACTIONS, etc.

    @Column(name = "resource_id")
    private String resourceId; // ID do recurso acessado

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "request_url")
    private String requestUrl;

    @Column(name = "response_status")
    private Integer responseStatus;
}
