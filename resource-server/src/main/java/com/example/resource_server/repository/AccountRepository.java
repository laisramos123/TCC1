package com.example.resource_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.resource_server.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByUserId(String userId);

    boolean existsByIdAndUserId(String accountId, String userId);
}
