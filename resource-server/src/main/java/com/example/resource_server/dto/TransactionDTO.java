package com.example.resource_server.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TransactionDTO {

    private String id;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String type; // DEBIT, CREDIT
    private String status;
    private LocalDateTime transactionDate;
    private String description;
    private String counterpartyName;
    private String counterpartyAccount;
    private String category;
}
