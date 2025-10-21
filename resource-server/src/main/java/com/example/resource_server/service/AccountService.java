package com.example.resource_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.resource_server.dto.AccountResponse;
import com.example.resource_server.model.Account;
import com.example.resource_server.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FASE 3 - PASSO 4: Lógica de negócio dos recursos
 */
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public AccountResponse getAccounts(String cpf) {

        List<Account> accounts = accountRepository.findByUserId(cpf);

        List<AccountResponse.AccountData> accountDataList = accounts.stream()
                .map(this::toAccountData)
                .collect(Collectors.toList());

        return AccountResponse.builder()
                .data(accountDataList)
                .links(AccountResponse.Links.builder()
                        .self("/open-banking/accounts/v2/accounts")
                        .build())
                .meta(AccountResponse.Meta.builder()
                        .totalRecords(accountDataList.size())
                        .totalPages(1)
                        .requestDateTime(LocalDateTime.now())
                        .build())
                .build();
    }

    public AccountResponse.AccountData getAccountById(String accountId, String cpf) {

        Account account = accountRepository.findByAccountIdAndUserId(accountId, cpf)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        return toAccountData(account);
    }

    public Map<String, Object> getAccountBalance(String accountId, String cpf) {

        Account account = accountRepository.findByAccountIdAndUserId(accountId, cpf)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        return Map.of(
                "availableAmount", account.getBalance(),
                "blockedAmount", BigDecimal.ZERO,
                "automaticallyInvestedAmount", BigDecimal.ZERO,
                "currency", account.getCurrency());
    }

    private AccountResponse.AccountData toAccountData(Account account) {
        return AccountResponse.AccountData.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .build();
    }
}