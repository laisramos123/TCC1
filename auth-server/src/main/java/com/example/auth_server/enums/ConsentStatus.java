package com.example.auth_server.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ConsentStatus {
    AWAITING_AUTHORISATION("AWAITING_AUTHORISATION"),
    AUTHORISED("AUTHORISED"),
    REJECTED("REJECTED"),
    CONSUMED("CONSUMED"),
    REVOKED("REVOKED");

    private final String value;

    ConsentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ConsentStatus fromValue(String value) {
        for (ConsentStatus status : ConsentStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status de consentimento inválido: " + value);
    }

    // Métodos de conveniência para regras de negócio
    public boolean isActive() {
        return this == AUTHORISED;
    }

    public boolean canBeRevoked() {
        return this == AUTHORISED || this == AWAITING_AUTHORISATION;
    }

    public boolean isTerminal() {
        return this == REJECTED || this == CONSUMED || this == REVOKED;
    }
}
