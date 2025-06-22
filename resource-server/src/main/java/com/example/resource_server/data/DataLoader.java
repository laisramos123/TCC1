package com.example.resource_server.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.resource_server.model.Account;
import com.example.resource_server.model.Transaction;
import com.example.resource_server.repository.AccountRepository;
import com.example.resource_server.repository.TransactionRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DataLoader(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        // Criar contas de exemplo
        Account account1 = new Account();
        account1.setAccountNumber("1234567890");
        account1.setAccountType("Corrente");
        account1.setCurrency("BRL");
        account1.setBalance(new BigDecimal("5000.00"));
        account1.setOwner("usuario");
        account1.setBank("Banco XYZ");
        account1.setBranch("0001");
        accountRepository.save(account1);

        Account account2 = new Account();
        account2.setAccountNumber("0987654321");
        account2.setAccountType("Poupança");
        account2.setCurrency("BRL");
        account2.setBalance(new BigDecimal("15000.00"));
        account2.setOwner("usuario");
        account2.setBank("Banco XYZ");
        account2.setBranch("0001");
        accountRepository.save(account2);

        // Criar transações de exemplo
        Transaction transaction1 = new Transaction();
        transaction1.setTransactionId(UUID.randomUUID().toString());
        transaction1.setAccount(account1);
        transaction1.setDate(LocalDateTime.now().minusDays(5));
        transaction1.setAmount(new BigDecimal("-150.00"));
        transaction1.setDescription("Supermercado ABC");
        transaction1.setCategory("Alimentação");
        transaction1.setType("DEBIT");
        transactionRepository.save(transaction1);

        Transaction transaction2 = new Transaction();
        transaction2.setTransactionId(UUID.randomUUID().toString());
        transaction2.setAccount(account1);
        transaction2.setDate(LocalDateTime.now().minusDays(3));
        transaction2.setAmount(new BigDecimal("-80.00"));
        transaction2.setDescription("Farmácia Popular");
        transaction2.setCategory("Saúde");
        transaction2.setType("DEBIT");
        transactionRepository.save(transaction2);

        Transaction transaction3 = new Transaction();
        transaction3.setTransactionId(UUID.randomUUID().toString());
        transaction3.setAccount(account1);
        transaction3.setDate(LocalDateTime.now().minusDays(1));
        transaction3.setAmount(new BigDecimal("3000.00"));
        transaction3.setDescription("Salário");
        transaction3.setCategory("Receita");
        transaction3.setType("CREDIT");
        transactionRepository.save(transaction3);
    }
}