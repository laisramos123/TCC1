package com.example.auth_server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth_server.model.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByClientId(String clientId);
}
