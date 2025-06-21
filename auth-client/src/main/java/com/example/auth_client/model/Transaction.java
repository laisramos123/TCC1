package com.example.auth_client.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Transaction {
    private Long id;
    private String transactionId;
    private Long accountId;
    private LocalDateTime date;
    private Double amount;
    private String description;
    private String category;
    private String type;
}
