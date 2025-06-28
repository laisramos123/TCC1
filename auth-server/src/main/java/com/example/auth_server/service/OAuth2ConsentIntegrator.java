package com.example.auth_server.service;

import java.util.Set;

import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2ConsentIntegrator {

    private final ConsentService consentService;
    private final OAuth2AuthorizationConsentService authorizationConsentService;
    private final RegisteredClientRepository registeredClientRepository;

    /**
     * Integra aprovação de consentimento com Spring Authorization Server
     * CHAMADO APENAS DEPOIS que o ConsentService já aprovou
     */
    public void integrateConsentApproval(String userId, String clientId, Set<String> scopes) {
        try {
            log.info("🔗 Integrando consentimento aprovado para userId: {}, clientId: {}", userId, clientId);

            // 1. Buscar cliente registrado
            RegisteredClient client = registeredClientRepository.findByClientId(clientId);
            if (client == null) {
                log.error("❌ Cliente não encontrado: {}", clientId);
                return;
            }

            // 2. Verificar se já existe consentimento OAuth2
            OAuth2AuthorizationConsent existingConsent = authorizationConsentService
                    .findById(clientId, userId);

            if (existingConsent != null) {
                log.info("♻️ Removendo consentimento OAuth2 existente");
                authorizationConsentService.remove(existingConsent);
            }

            // 3. Criar novo consentimento OAuth2
            OAuth2AuthorizationConsent.Builder consentBuilder = OAuth2AuthorizationConsent
                    .withId(clientId, userId);

            // 4. Adicionar scopes aprovados
            scopes.forEach(scope -> {
                if (client.getScopes().contains(scope)) {
                    consentBuilder.scope(scope);
                    log.debug("➕ Scope adicionado: {}", scope);
                } else {
                    log.warn("⚠️ Scope '{}' não está registrado para o cliente '{}'", scope, clientId);
                }
            });

            OAuth2AuthorizationConsent consent = consentBuilder.build();

            // 5. Salvar consentimento no Spring Authorization Server
            authorizationConsentService.save(consent);

            log.info("✅ Consentimento OAuth2 integrado com sucesso para userId: {}", userId);

        } catch (Exception e) {
            log.error("💥 Erro ao integrar consentimento OAuth2", e);
        }
    }

    /**
     * Remove consentimento tanto do ConsentService quanto do OAuth2
     */
    public void revokeConsent(String userId, String clientId, String consentId) {
        try {
            log.info("🚫 Revogando consentimento para userId: {}, clientId: {}", userId, clientId);

            // 1. Revogar no ConsentService
            consentService.revokeConsent(consentId);

            // 2. Remover do OAuth2AuthorizationConsentService
            OAuth2AuthorizationConsent existingConsent = authorizationConsentService
                    .findById(clientId, userId);

            if (existingConsent != null) {
                authorizationConsentService.remove(existingConsent);
                log.info("✅ Consentimento OAuth2 removido");
            }

        } catch (Exception e) {
            log.error("💥 Erro ao revogar consentimento", e);
        }
    }
}