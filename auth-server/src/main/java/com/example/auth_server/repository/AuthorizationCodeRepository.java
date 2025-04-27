package com.example.auth_server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth_server.model.AuthorizationCode;

public interface AuthorizationCodeRepository extends JpaRepository<AuthorizationCode, String> {

    Optional<AuthorizationCode> findByAuthorizationCode(String authorizationCode);

    void deleteByAuthorizationCode(String authorizationCode);

}
