package com.example.resource_server.enums;

public enum ConsentValidationStatus {
    AUTHORISED("Autorizado"),
    REJECTED("Rejeitado"),
    EXPIRED("Expirado");

    private final String description;

    ConsentValidationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
