package com.example.resource_server.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.resource_server.model.Account;
import com.example.resource_server.repository.AccountRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByOwner(String owner) {
        return accountRepository.findByOwner(owner);
    }

    @Transactional(readOnly = true)
    public Account getAccountById(Long id, String owner) {
        return accountRepository.findById(id)
                .filter(account -> account.getOwner().equals(owner))
                .orElseThrow(() -> new RuntimeException("Conta não encontrada ou acesso não autorizado"));
    }
}