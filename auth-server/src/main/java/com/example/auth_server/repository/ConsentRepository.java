package com.example.auth_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.auth_server.enums.ConsentStatus;
import com.example.auth_server.model.Consent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, String> {

    List<Consent> findByLoggedUserDocument(String document);

    List<Consent> findByStatus(ConsentStatus status);

    List<Consent> findByStatusAndExpirationDateTimeBefore(
            ConsentStatus status,
            LocalDateTime dateTime);

    Optional<Consent> findByConsentIdAndStatus(String consentId, ConsentStatus status);
}