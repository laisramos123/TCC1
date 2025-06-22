package com.example.resource_server.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extrair claims específicas do Open Finance que podem ser necessárias depois
        // Por exemplo, o consent_id que identifica o consentimento do usuário

        // Extrair os escopos do token
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        // Criar um token de autenticação personalizado
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> scopes = jwt.getClaimAsStringList("scope");
        if (scopes == null || scopes.isEmpty()) {
            return Collections.emptyList();
        }

        return scopes.stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .collect(Collectors.toList());
    }
}