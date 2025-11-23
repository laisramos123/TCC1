package com.example.auth_server.service;

import com.example.auth_server.dto.*;
import com.example.auth_server.model.Consent;
import com.example.auth_server.enums.ConsentStatus;
import com.example.auth_server.repository.ConsentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConsentService {
    private static final Logger log = LoggerFactory.getLogger(ConsentService.class);
    
    @Autowired
    private ConsentRepository consentRepository;
    
    private final Map<String, Consent> activeConsents = new HashMap<>();
    
    public ConsentResponse createConsent(ConsentRequest request) {
        log.info("Criando novo consentimento");
        
        try {
            Consent consent = new Consent();
            consent.setConsentId(UUID.randomUUID().toString());
            consent.setClientId("oauth-client");
            consent.setStatus(ConsentStatus.AWAITING_AUTHORISATION);
            consent.setCreationDateTime(LocalDateTime.now());
            consent.setExpirationDateTime(LocalDateTime.now().plusDays(90));
            
            if (request.getData() != null) {
                consent.setPermissions(request.getData().getPermissions());
                consent.setLoggedUserDocument(request.getData().getLoggedUser());
            }
            
            consent = consentRepository.save(consent);
            activeConsents.put(consent.getConsentId(), consent);
            
            log.info("Consentimento criado: {}", consent.getConsentId());
            return buildConsentResponse(consent);
            
        } catch (Exception e) {
            log.error("Erro ao criar consentimento", e);
            throw new RuntimeException("Falha ao criar consentimento", e);
        }
    }
    
    public boolean validateConsentForAuthorization(String consentId) {
        log.info("Validando consentimento: {}", consentId);
        
        Consent consent = activeConsents.get(consentId);
        if (consent == null) {
            consent = consentRepository.findById(consentId).orElse(null);
        }
        
        if (consent == null) {
            log.warn("Consentimento não encontrado: {}", consentId);
            return false;
        }
        
        boolean valid = consent.getStatus() == ConsentStatus.AUTHORISED 
                     && consent.getExpirationDateTime().isAfter(LocalDateTime.now());
        
        log.info("Consentimento {} válido: {}", consentId, valid);
        return valid;
    }
    
    public void updateStatus(String consentId, ConsentStatus newStatus) {
        log.info("Atualizando status do consentimento {} para {}", consentId, newStatus);
        
        Consent consent = consentRepository.findById(consentId)
            .orElseThrow(() -> new RuntimeException("Consentimento não encontrado"));
        
        consent.setStatus(newStatus);
        consent.setStatusUpdateDateTime(LocalDateTime.now());
        
        if (newStatus == ConsentStatus.AUTHORISED) {
            consent.setStatusUpdateDateTime(LocalDateTime.now());
        }
        
        consentRepository.save(consent);
        activeConsents.put(consentId, consent);
        
        log.info("Status atualizado com sucesso");
    }
    
    public void revokeConsent(String consentId, String reason, String revokedBy) {
        log.info("Revogando consentimento: {} por {}", consentId, reason);
        
        Consent consent = consentRepository.findById(consentId)
            .orElseThrow(() -> new RuntimeException("Consentimento não encontrado"));
        
        consent.setStatus(ConsentStatus.REJECTED);
        consent.setRevokedAt(LocalDateTime.now());
        consent.setRevocationReasonCode(reason);
        consent.setRevokedBy(revokedBy);
        
        consentRepository.save(consent);
        activeConsents.remove(consentId);
        
        log.info("Consentimento revogado com sucesso");
    }
    
    public List<ConsentResponse> listConsents(String clientId) {
        log.info("Listando consentimentos");
        
        List<Consent> consents = consentRepository.findByStatus(ConsentStatus.AUTHORISED);
        
        return consents.stream()
            .map(this::buildConsentResponse)
            .collect(Collectors.toList());
    }
    
    public ConsentResponse getConsent(String consentId) {
        log.info("Buscando consentimento: {}", consentId);
        
        Consent consent = consentRepository.findById(consentId)
            .orElseThrow(() -> new RuntimeException("Consentimento não encontrado"));
        
        return buildConsentResponse(consent);
    }
    
    private ConsentResponse buildConsentResponse(Consent consent) {
        ConsentResponse response = new ConsentResponse();
        
        ConsentResponse.Data data = new ConsentResponse.Data();
        data.setConsentId(consent.getConsentId());
        response.setData(data);
        
        return response;
    }
    
    public boolean validatePermissions(String consentId, List<String> requiredPermissions) {
        Consent consent = consentRepository.findById(consentId).orElse(null);
        
        if (consent == null || consent.getStatus() != ConsentStatus.AUTHORISED) {
            return false;
        }
        
        List<String> grantedPermissions = consent.getPermissions();
        return grantedPermissions != null && grantedPermissions.containsAll(requiredPermissions);
    }
}
