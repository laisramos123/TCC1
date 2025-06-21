package com.example.auth_client.model;

import lombok.Data;

@Data
public class Account {
    private Long id;
    private String accountNumber;
    private String accountType;
    private String currency;
    private Double balance;
    private String owner;
    private String bank;
    private String branch;
}
