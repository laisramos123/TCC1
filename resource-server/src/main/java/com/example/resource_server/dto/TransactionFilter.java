package com.example.resource_server.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class TransactionFilter {
    private String accountId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
}
