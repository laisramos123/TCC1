package com.example.resource_server.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.example.resource_server.enums.LoanType;

@Table(name = "loans")
public class Loan {
    @Id
    @Column(name = "contract_id")
    private String contractId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "contract_number", nullable = false)
    private String contractNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LoanType type;

    @Column(name = "contract_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal contractAmount;

    @Column(name = "currency", nullable = false)
    private String currency; // BRL

    @Column(name = "contract_date", nullable = false)
    private LocalDate contractDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    // Getters, Setters, Constructors...
}
