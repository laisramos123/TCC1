package com.example.resource_server.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "credit_card_limits")
public class CreditCardLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_card_account_id", nullable = false)
    private String creditCardAccountId;

    @Column(name = "consolidated_limit_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal consolidatedLimitAmount;

    @Column(name = "used_limit_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal usedLimitAmount;

    @Column(name = "available_limit_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal availableLimitAmount;

    @Column(name = "reference_date", nullable = false)
    private LocalDate referenceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_account_id", insertable = false, updatable = false)
    private CreditCardAccount creditCardAccount;

    // Getters, Setters, Constructors...
}
