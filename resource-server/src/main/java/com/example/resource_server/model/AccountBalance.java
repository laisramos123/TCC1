package com.example.resource_server.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "account_balances", indexes = {
        @Index(name = "idx_balance_account", columnList = "account_id"),
        @Index(name = "idx_balance_reference_date", columnList = "reference_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false, length = 100)
    private String accountId;

    @Column(name = "available_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal availableAmount;

    @Column(name = "blocked_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal blockedAmount;

    @Column(name = "automatically_invested_amount", precision = 19, scale = 4)
    private BigDecimal automaticallyInvestedAmount;

    @Column(name = "reference_date", nullable = false)
    private LocalDate referenceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Account account;

    public BigDecimal getTotalAmount() {
        BigDecimal total = availableAmount.add(blockedAmount);
        if (automaticallyInvestedAmount != null) {
            total = total.add(automaticallyInvestedAmount);
        }
        return total;
    }
}
