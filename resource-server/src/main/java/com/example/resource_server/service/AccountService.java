package com.example.resource_server.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.resource_server.dto.AccountDTO;
import com.example.resource_server.exceptions.ResourceNotFoundException;
import com.example.resource_server.mapper.AccountMapper;
import com.example.resource_server.model.Account;
import com.example.resource_server.model.Balance;
import com.example.resource_server.repository.AccountRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Autowired
    public AccountService(AccountRepository accountRepository,
            AccountMapper accountMapper) {
        this.accountRepository = accountRepository;

        this.accountMapper = accountMapper;
    }

    public List<AccountDTO> findAccountsByUserId(String userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        return accounts.stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    public AccountDTO findAccountById(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        return accountMapper.toDto(account);
    }

    public boolean hasAccessToAccount(String userId, String accountId) {
        // 1. Verifica se a conta pertence ao usuário
        boolean isOwner = accountRepository.existsByIdAndUserId(accountId, userId);

        if (isOwner) {
            return true;
        }

        // 2. Verifica se existe um consentimento ativo que dá acesso a esta conta
        // Lógica mais complexa aqui, dependendo da sua implementação de consentimentos
        return false;
    }

}
