package com.example.auth_server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSession;

@Component
public class CustomAuthorizationService implements OAuth2AuthorizationService {

    private final InMemoryOAuth2AuthorizationService delegate;
    private final HttpSession session;

    @Autowired
    public CustomAuthorizationService(HttpSession session) {
        this.delegate = new InMemoryOAuth2AuthorizationService();
        this.session = session;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        // Captura o ID do consentimento da sessão e adiciona à autorização
        String consentId = (String) session.getAttribute("CONSENT_ID");
        if (consentId != null) {
            OAuth2Authorization.Builder builder = OAuth2Authorization.from(authorization);
            builder.attribute("consent_id", consentId);

            // Remove da sessão para evitar reutilização
            session.removeAttribute("CONSENT_ID");

            delegate.save(builder.build());
        } else {
            delegate.save(authorization);
        }
    }

    // Implementa os outros métodos da interface OAuth2AuthorizationService
    // delegando para a implementação padrão
    @Override
    public void remove(OAuth2Authorization authorization) {
        delegate.remove(authorization);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return delegate.findById(id);
    }

    @Override
    public OAuth2Authorization findByToken(String token,
            OAuth2TokenType tokenType) {
        return delegate.findByToken(token, tokenType);
    }
}
