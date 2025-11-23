package com.example.resource_server.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String transactionId;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "BRL";

    @Column(nullable = false)
    private LocalDateTime transactionDateTime;

    @Column(nullable = false)
    private String description;

    private String counterpartyName;

    @Column(nullable = false)
    private String status = "COMPLETED";

    @PrePersist
    protected void onCreate() {
        if (transactionDateTime == null) {
            transactionDateTime = LocalDateTime.now();
        }
    }
}
