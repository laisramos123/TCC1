package com.example.resource_server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.resource_server.model.Consent;

public interface ConsentRepository extends JpaRepository<Consent, Long> {
    Optional<Consent> findByConsentId(String consentId);

    Optional<Consent> findByUserIdAndClientIdAndStatusAndRevokedAtIsNull(
            String userId, String clientId, String status);
}