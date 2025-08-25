package com.example.resource_server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "credit_card_accounts")
public class CreditCardAccount {
    @Id
    @Column(name = "credit_card_account_id")
    private String creditCardAccountId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "product_type", nullable = false)
    private String productType;

    @Column(name = "credit_card_network", nullable = false)
    private String creditCardNetwork;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @OneToMany(mappedBy = "creditCardAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CreditCardLimit> limits = new ArrayList<>();

    // Getters, Setters, Constructors...
}
