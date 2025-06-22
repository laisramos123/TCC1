package com.example.resource_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.resource_server.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
        List<Transaction> findByAccount_Owner(String owner);
}