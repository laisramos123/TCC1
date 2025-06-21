package com.example.resource_server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String type; // DEBIT, CREDIT

    @Column(nullable = false)
    private String status; // COMPLETED, PENDING, CANCELED

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "counterparty_name")
    private String counterpartyName;

    @Column(name = "counterparty_account")
    private String counterpartyAccount;

    @Column(name = "counterparty_bank")
    private String counterpartyBank;

    private String category; // FOOD, TRANSPORT, etc.

    @Column(name = "transaction_code", unique = true)
    private String transactionCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
