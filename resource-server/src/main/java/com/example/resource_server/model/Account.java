package com.example.resource_server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import com.example.resource_server.enums.AccountType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_account_customer", columnList = "customer_id"),
        @Index(name = "idx_account_number", columnList = "branch_code,number,check_digit", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(name = "account_id", length = 100)
    private String accountId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "branch_code", nullable = false, length = 10)
    private String branchCode;

    @Column(name = "number", nullable = false, length = 20)
    private String number;

    @Column(name = "check_digit", nullable = false, length = 2)
    private String checkDigit;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "subtype", length = 30)
    private AccountType subtype;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency; // BRL

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("referenceDate DESC")
    @Builder.Default
    private List<AccountBalance> balances = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("completionDateTime DESC")
    @Builder.Default
    private List<AccountTransaction> transactions = new ArrayList<>();

    public String getFormattedAccountNumber() {
        return String.format("%s-%s-%s", branchCode, number, checkDigit);
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
