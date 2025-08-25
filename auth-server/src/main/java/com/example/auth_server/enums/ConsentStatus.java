package com.example.auth_server.enums;

public enum ConsentStatus {
    AWAITING_AUTHORISATION("Aguardando autorização do usuário"),
    AUTHORISED("Autorizado pelo usuário"),
    REJECTED("Rejeitado pelo usuário ou sistema");

    private final String description;

    ConsentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
