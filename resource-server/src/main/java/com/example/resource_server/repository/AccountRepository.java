package com.example.resource_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.resource_server.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByOwner(String owner);
}