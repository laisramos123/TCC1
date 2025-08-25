package com.example.auth_server.model;

import java.io.Serializable;

import com.example.auth_server.enums.Permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PermissionGroupItemId implements Serializable {
    private String groupName;
    private Permission requiredPermission;
}
