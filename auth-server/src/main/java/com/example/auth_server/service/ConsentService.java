package com.example.auth_server.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.auth_server.model.Consent;
import com.example.auth_server.repository.ConsentRepository;

@Service
public class ConsentService {

    @Autowired
    private ConsentRepository consentRepository;

    public String createConsent(String clientId, String scope) {
        Consent consent = new Consent();
        consent.setConsentId(UUID.randomUUID().toString());
        consent.setClientId(clientId);
        consent.setScope(scope);
        consent.setStatus("AWAITING_AUTHORIZATION");
        consent.setCreatedAt(LocalDateTime.now());
        consent.setUpdatedAt(LocalDateTime.now().plusDays(90));
        consentRepository.save(consent);
        return consent.getConsentId();
    }

    public void approveConsent(String consentId) {
        Optional<Consent> consent = consentRepository.findByConsentId(consentId);
        if (consent.isPresent()) {
            Consent existingConsent = consent.get();
            existingConsent.setStatus("AUTHORIZED");
            existingConsent.setUpdatedAt(LocalDateTime.now());
            consentRepository.save(existingConsent);
        }
    }

    public void revokeConsent(String consentId) {
        Optional<Consent> consent = consentRepository.findByConsentId(consentId);
        if (consent.isPresent()) {
            Consent existingConsent = consent.get();
            existingConsent.setStatus("REVOKED");
            existingConsent.setUpdatedAt(LocalDateTime.now());
            consentRepository.save(existingConsent);
        }
    }
}
