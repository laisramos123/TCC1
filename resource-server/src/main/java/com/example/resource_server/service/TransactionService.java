package com.example.resource_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.resource_server.model.Transaction;
import com.example.resource_server.repository.TransactionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

        @Autowired
        private TransactionRepository transactionRepository;

        @Autowired
        private AccountService accountService;

        public List<Transaction> findTransactions(
                        String accountId,
                        String cpf,
                        LocalDate fromDate,
                        LocalDate toDate,
                        int page,
                        int pageSize) {

                accountService.getAccountById(accountId, cpf);

                LocalDateTime from = fromDate != null
                                ? fromDate.atStartOfDay()
                                : LocalDateTime.now().minusMonths(1);

                LocalDateTime to = toDate != null
                                ? toDate.atTime(23, 59, 59)
                                : LocalDateTime.now();

                return transactionRepository.findByAccountIdAndTransactionDateTimeBetween(
                                accountId,
                                from,
                                to,
                                PageRequest.of(page - 1, pageSize));
        }
}