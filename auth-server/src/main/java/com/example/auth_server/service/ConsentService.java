package com.example.auth_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_server.dto.ConsentRequest;
import com.example.auth_server.dto.ConsentResponse;
import com.example.auth_server.enums.ConsentStatus;
import com.example.auth_server.model.Consent;
import com.example.auth_server.repository.ConsentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConsentService {

    @Autowired
    private ConsentRepository consentRepository;

    @Transactional
    public ConsentResponse createConsent(ConsentRequest request) {

        validateConsentRequest(request);

        String consentId = "urn:banco:" + UUID.randomUUID().toString();

        Consent consent = Consent.builder()
                .consentId(consentId)
                .loggedUserDocument(request.getData().getLoggedUser().getDocument().getIdentification())
                .loggedUserRel(request.getData().getLoggedUser().getDocument().getRel())
                .businessEntityDocument(request.getData().getBusinessEntity().getDocument().getIdentification())
                .businessEntityRel(request.getData().getBusinessEntity().getDocument().getRel())
                .permissions(request.getData().getPermissions())
                .status(ConsentStatus.AWAITING_AUTHORISATION)
                .creationDateTime(LocalDateTime.now())
                .statusUpdateDateTime(LocalDateTime.now())
                .expirationDateTime(request.getData().getExpirationDateTime())
                .transactionFromDateTime(request.getData().getTransactionFromDateTime())
                .transactionToDateTime(request.getData().getTransactionToDateTime())
                .build();

        consent = consentRepository.save(consent);

        return toConsentResponse(consent);
    }

    public ConsentResponse getConsent(String consentId) {
        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consentimento não encontrado"));

        return toConsentResponse(consent);
    }

    @Transactional
    public ConsentResponse updateStatus(String consentId, ConsentStatus newStatus) {

        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consentimento não encontrado"));

        consent.setStatus(newStatus);
        consent.setStatusUpdateDateTime(LocalDateTime.now());

        consent = consentRepository.save(consent);

        return toConsentResponse(consent);
    }

    @Transactional
    public void revokeConsent(String consentId, String reasonCode, String revokedBy) {

        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consentimento não encontrado"));

        consent.setStatus(ConsentStatus.REVOKED);
        consent.setRevocationReasonCode(reasonCode);
        consent.setRevocationReasonDetail(getRevocationReasonDetail(reasonCode));
        consent.setRevokedBy(revokedBy);
        consent.setRevokedAt(LocalDateTime.now());

        consentRepository.save(consent);
    }

    public void validateConsentForAuthorization(String consentId) {

        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consentimento não encontrado"));

        if (consent.getStatus() != ConsentStatus.AWAITING_AUTHORISATION) {
            throw new RuntimeException(
                    "Consentimento não está aguardando autorização. Status: " + consent.getStatus());
        }

        if (consent.getExpirationDateTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Consentimento expirado");
        }
    }

    public void validateConsentForResourceAccess(String consentId, String requiredPermission) {

        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new RuntimeException("Consentimento não encontrado"));

        if (consent.getStatus() != ConsentStatus.AUTHORISED) {
            throw new RuntimeException("Consentimento não está autorizado");
        }

        if (consent.getExpirationDateTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Consentimento expirado");
        }

        if (!consent.getPermissions().contains(requiredPermission)) {
            throw new RuntimeException(
                    "Consentimento não possui a permissão: " + requiredPermission);
        }
    }

    public List<ConsentResponse> listConsents(String cpf) {
        return consentRepository.findByLoggedUserDocument(cpf)
                .stream()
                .map(this::toConsentResponse)
                .collect(Collectors.toList());
    }

    private void validateConsentRequest(ConsentRequest request) {

        if (request.getData() == null) {
            throw new IllegalArgumentException("data é obrigatório");
        }

        ConsentRequest.Data data = request.getData();

        if (data.getLoggedUser() == null ||
                data.getLoggedUser().getDocument() == null ||
                data.getLoggedUser().getDocument().getIdentification() == null) {
            throw new IllegalArgumentException("loggedUser.document.identification é obrigatório");
        }

        if (data.getPermissions() == null || data.getPermissions().isEmpty()) {
            throw new IllegalArgumentException("permissions é obrigatório");
        }

        if (data.getExpirationDateTime() == null) {
            throw new IllegalArgumentException("expirationDateTime é obrigatório");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxExpiration = now.plusDays(60);

        if (data.getExpirationDateTime().isBefore(now)) {
            throw new IllegalArgumentException("expirationDateTime não pode ser no passado");
        }

        if (data.getExpirationDateTime().isAfter(maxExpiration)) {
            throw new IllegalArgumentException("expirationDateTime não pode ser superior a 60 dias");
        }
    }

    private ConsentResponse toConsentResponse(Consent consent) {

        return ConsentResponse.builder()
                .data(ConsentResponse.Data.builder()
                        .consentId(consent.getConsentId())
                        .creationDateTime(consent.getCreationDateTime())
                        .status(consent.getStatus().name())
                        .statusUpdateDateTime(consent.getStatusUpdateDateTime())
                        .expirationDateTime(consent.getExpirationDateTime())
                        .permissions(consent.getPermissions())
                        .loggedUser(ConsentResponse.LoggedUser.builder()
                                .document(ConsentResponse.Document.builder()
                                        .identification(consent.getLoggedUserDocument())
                                        .rel(consent.getLoggedUserRel())
                                        .build())
                                .build())
                        .businessEntity(ConsentResponse.BusinessEntity.builder()
                                .document(ConsentResponse.Document.builder()
                                        .identification(consent.getBusinessEntityDocument())
                                        .rel(consent.getBusinessEntityRel())
                                        .build())
                                .build())
                        .transactionFromDateTime(consent.getTransactionFromDateTime())
                        .transactionToDateTime(consent.getTransactionToDateTime())
                        .build())
                .links(ConsentResponse.Links.builder()
                        .self("/open-banking/consents/v2/consents/" + consent.getConsentId())
                        .build())
                .meta(ConsentResponse.Meta.builder()
                        .totalRecords(1)
                        .totalPages(1)
                        .requestDateTime(LocalDateTime.now())
                        .build())
                .build();
    }

    private String getRevocationReasonDetail(String code) {
        return switch (code) {
            case "CUSTOMER_MANUALLY_REVOKED" -> "Cliente revogou manualmente";
            case "TPP_REQUESTED" -> "TPP solicitou revogação";
            case "FRAUD_SUSPECTED" -> "Suspeita de fraude";
            default -> "Razão não especificada";
        };
    }
}
