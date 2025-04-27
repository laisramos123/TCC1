package com.example.auth_server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.auth_server.model.Consent;

public interface ConsentRepository extends JpaRepository<Consent, String> {
    Optional<Consent> findByConsentId(String consentId);

    Consent findByClientIdAndScope(String clientId, String scope);

    void deleteByConsentId(String consentId);

    void deleteByClientIdAndScope(String clientId, String scope);

}
