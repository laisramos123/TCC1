package com.example.auth_server.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.example.auth_server.enums.Permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permission_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionGroup {

    @Id
    @Column(name = "name", length = 50)
    private String name; // DADOS_CADASTRAIS_PF

    @Column(name = "category", nullable = false, length = 50)
    private String category; // Cadastro, Contas, Credito

    @Column(name = "description", length = 500)
    private String description;

    @OneToMany(mappedBy = "permissionGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<PermissionGroupItem> requiredPermissions = new HashSet<>();

    public void addRequiredPermission(Permission permission) {
        PermissionGroupItem item = new PermissionGroupItem();
        item.setGroupName(this.name);
        item.setRequiredPermission(permission);
        item.setPermissionGroup(this);
        this.requiredPermissions.add(item);
    }
}
