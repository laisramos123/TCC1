package com.example.auth_server.dto;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

import com.example.auth_server.enums.Permission;

@Embeddable
public class ConsentPermissionId implements Serializable {
    private String consentId;
    private Permission permission;

    // Equals, HashCode, Getters, Setters...
}
