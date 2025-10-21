package com.example.resource_server.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Conversor que extrai authorities dos scopes do JWT
 */
public class JwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        String principalClaimValue = jwt.getClaimAsString("sub");

        return new JwtAuthenticationToken(jwt, authorities, principalClaimValue);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {

        String scopeString = jwt.getClaimAsString("scope");
        if (scopeString == null) {
            scopeString = jwt.getClaimAsString("scp");
        }

        if (scopeString == null || scopeString.isEmpty()) {
            return List.of();
        }

        return List.of(scopeString.split(" "))
                .stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .collect(Collectors.toList());
    }
}