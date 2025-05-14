package com.example.resource_server.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.resource_server.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String>,
        JpaSpecificationExecutor<Transaction> {
    List<Transaction> findByAccountId(String accountId);

    Page<Transaction> findByAccountIdAndTransactionDateBetween(
            String accountId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}