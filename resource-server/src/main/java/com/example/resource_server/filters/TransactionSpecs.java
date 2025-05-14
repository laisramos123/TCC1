package com.example.resource_server.filters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.jpa.domain.Specification;

import com.example.resource_server.model.Transaction;

public class TransactionSpecs {

    public static Specification<Transaction> hasAccountId(String accountId) {
        return (root, query, cb) -> cb.equal(root.get("accountId"), accountId);
    }

    public static Specification<Transaction> afterDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("transactionDate"), startOfDay);
    }

    public static Specification<Transaction> beforeDate(LocalDate date) {
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("transactionDate"), endOfDay);
    }

    public static Specification<Transaction> hasType(String type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }
}
