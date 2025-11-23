package com.example.auth_server.enums;

public enum ConsentStatus {
    AWAITING_AUTHORISATION,
    AUTHORISED,  // Mudando para AUTHORISED (padrão UK do Open Banking)
    REJECTED,
    CONSUMED,
    REVOKED,
    EXPIRED,
    AWAITING_AUTHORIZATION  // Mantendo para compatibilidade
}
