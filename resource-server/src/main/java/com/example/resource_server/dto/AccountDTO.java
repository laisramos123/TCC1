package com.example.resource_server.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AccountDTO {
    private String id;
    private String number;
    private String agency;
    private String type;
    private String name;
    private String status;
    private String currency;
    private LocalDate openingDate;
}
