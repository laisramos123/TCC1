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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

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
            consent.setClientId(request.getClientId() != null ? request.getClientId() : "oauth-client");
            consent.setStatus(ConsentStatus.AWAITING_AUTHORISATION);
            consent.setCreationDateTime(LocalDateTime.now());
            consent.setExpirationDateTime(LocalDateTime.now().plusDays(90));

            if (request.getData() != null) {
                consent.setPermissions(request.getData().getPermissions());

                if (request.getData().getLoggedUser() != null &&
                        request.getData().getLoggedUser().getDocument() != null) {
                    consent.setLoggedUserDocument(
                            request.getData().getLoggedUser().getDocument().getIdentification());
                    consent.setLoggedUserRel(
                            request.getData().getLoggedUser().getDocument().getRel());
                }

                if (request.getData().getBusinessEntity() != null &&
                        request.getData().getBusinessEntity().getDocument() != null) {
                    consent.setBusinessEntityDocument(
                            request.getData().getBusinessEntity().getDocument().getIdentification());
                    consent.setBusinessEntityRel(
                            request.getData().getBusinessEntity().getDocument().getRel());
                }

                if (request.getData().getExpirationDateTime() != null) {
                    consent.setExpirationDateTime(request.getData().getExpirationDateTime());
                }
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

    @Cacheable(value = "consentValidations", key = "#consentId")
    public boolean validateConsentForAuthorization(String consentId) {
        log.info("Validando consentimento: {}", consentId);

        if (consentId == null || consentId.trim().isEmpty()) {
            log.warn("Consent ID inválido");
            return false;
        }

        Consent consent = activeConsents.get(consentId);
        if (consent == null) {
            consent = consentRepository.findById(consentId).orElse(null);
        }

        if (consent == null) {
            log.warn("Consentimento não encontrado: {}", consentId);
            return false;
        }

        boolean valid = (consent.getStatus() == ConsentStatus.AUTHORISED ||
                consent.getStatus() == ConsentStatus.AWAITING_AUTHORISATION) &&
                consent.getExpirationDateTime().isAfter(LocalDateTime.now());

        log.info("Consentimento {} válido: {}", consentId, valid);
        return valid;
    }

    @CacheEvict(value = { "consentValidations", "consents" }, key = "#consentId")
    public void updateStatus(String consentId, ConsentStatus newStatus) {
        log.info("Atualizando status do consentimento {} para {}", consentId, newStatus);

        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consentimento não encontrado: " + consentId));

        consent.setStatus(newStatus);
        consent.setStatusUpdateDateTime(LocalDateTime.now());

        consentRepository.save(consent);

        if (newStatus == ConsentStatus.AUTHORISED) {
            activeConsents.put(consentId, consent);
        } else if (newStatus == ConsentStatus.REJECTED ||
                newStatus == ConsentStatus.REVOKED ||
                newStatus == ConsentStatus.EXPIRED) {
            activeConsents.remove(consentId);
        }

        log.info("Status atualizado com sucesso");
    }

    @CacheEvict(value = { "consentValidations", "consents" }, key = "#consentId")
    public void revokeConsent(String consentId, String reason, String revokedBy) {
        log.info("Revogando consentimento: {} por {}", consentId, reason);

        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consentimento não encontrado: " + consentId));

        consent.setStatus(ConsentStatus.REVOKED);
        consent.setRevokedAt(LocalDateTime.now());
        consent.setRevocationReasonCode(reason);
        consent.setRevokedBy(revokedBy);
        consent.setStatusUpdateDateTime(LocalDateTime.now());

        consentRepository.save(consent);
        activeConsents.remove(consentId);

        log.info("Consentimento revogado com sucesso");
    }

    public List<ConsentResponse> listConsents(String clientId) {
        log.info("Listando consentimentos para cliente: {}", clientId);

        List<Consent> consents;

        if (clientId != null && !clientId.trim().isEmpty()) {
            consents = consentRepository.findByClientId(clientId);
        } else {
            consents = consentRepository.findByStatus(ConsentStatus.AUTHORISED);
        }

        return consents.stream()
                .map(this::buildConsentResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "consents", key = "#consentId")
    public ConsentResponse getConsent(String consentId) {
        log.info("Buscando consentimento: {}", consentId);

        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consentimento não encontrado: " + consentId));

        return buildConsentResponse(consent);
    }

    private ConsentResponse buildConsentResponse(Consent consent) {
        ConsentResponse response = new ConsentResponse();

        ConsentResponse.Data data = new ConsentResponse.Data();
        data.setConsentId(consent.getConsentId());
        data.setStatus(consent.getStatus().toString());
        data.setCreationDateTime(consent.getCreationDateTime());
        data.setStatusUpdateDateTime(consent.getStatusUpdateDateTime());
        data.setExpirationDateTime(consent.getExpirationDateTime());
        data.setPermissions(consent.getPermissions());

        if (consent.getLoggedUserDocument() != null) {
            ConsentResponse.LoggedUser loggedUser = new ConsentResponse.LoggedUser();
            ConsentResponse.Document loggedUserDoc = new ConsentResponse.Document();
            loggedUserDoc.setIdentification(consent.getLoggedUserDocument());
            loggedUserDoc.setRel(consent.getLoggedUserRel());
            loggedUser.setDocument(loggedUserDoc);
            data.setLoggedUser(loggedUser);
        }

        if (consent.getBusinessEntityDocument() != null) {
            ConsentResponse.BusinessEntity businessEntity = new ConsentResponse.BusinessEntity();
            ConsentResponse.Document businessDoc = new ConsentResponse.Document();
            businessDoc.setIdentification(consent.getBusinessEntityDocument());
            businessDoc.setRel(consent.getBusinessEntityRel());
            businessEntity.setDocument(businessDoc);
            data.setBusinessEntity(businessEntity);
        }

        response.setData(data);

        ConsentResponse.Links links = new ConsentResponse.Links();
        links.setSelf("/open-banking/consents/v2/consents/" + consent.getConsentId());
        response.setLinks(links);

        ConsentResponse.Meta meta = new ConsentResponse.Meta();
        meta.setTotalRecords(1);
        meta.setTotalPages(1);
        meta.setRequestDateTime(LocalDateTime.now());
        response.setMeta(meta);

        return response;
    }

    public boolean validatePermissions(String consentId, List<String> requiredPermissions) {
        Consent consent = consentRepository.findById(consentId).orElse(null);

        if (consent == null || consent.getStatus() != ConsentStatus.AUTHORISED) {
            return false;
        }

        if (consent.getExpirationDateTime().isBefore(LocalDateTime.now())) {

            updateStatus(consentId, ConsentStatus.EXPIRED);
            return false;
        }

        List<String> grantedPermissions = consent.getPermissions();
        return grantedPermissions != null && grantedPermissions.containsAll(requiredPermissions);
    }

    public void validateConsentForResourceAccess(String consentId, String resource) {
        if (!validateConsentForAuthorization(consentId)) {
            throw new RuntimeException("Consentimento inválido ou não autorizado");
        }

        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consentimento não encontrado"));

        if (consent.getPermissions() == null || !consent.getPermissions().contains(resource)) {
            throw new RuntimeException("Consentimento não possui permissão para: " + resource);
        }
    }
}