package com.example.resource_server.model;

import java.io.Serializable;

import com.example.resource_server.enums.Permission;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ConsentValidationPermissionId implements Serializable {
    private String consentId;
    private Permission permission;
}
