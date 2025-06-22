package com.example.resource_server.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.resource_server.model.Transaction;
import com.example.resource_server.repository.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByOwner(String owner) {
        return transactionRepository.findByAccount_Owner(owner);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByDate(String owner, LocalDateTime startDate, LocalDateTime endDate) {
        // Aqui você poderia adicionar um método personalizado ao repository
        // ou filtrar a lista retornada
        return transactionRepository.findByAccount_Owner(owner).stream()
                .filter(tx -> !tx.getDate().isBefore(startDate) && !tx.getDate().isAfter(endDate))
                .toList();
    }
}