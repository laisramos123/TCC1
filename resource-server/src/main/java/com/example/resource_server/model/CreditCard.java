package com.example.resource_server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String cardId;

    @Column(nullable = false)
    private String userId; // CPF do titular

    @Column(nullable = false)
    private String cardNumber; // Mascarado: **** **** **** 1234

    @Column(nullable = false)
    private String cardHolderName;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private String cardType; // CREDITO, DEBITO

    @Column(nullable = false)
    private String brand; // VISA, MASTERCARD, ELO

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal creditLimit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableLimit;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}