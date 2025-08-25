package com.example.auth_server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.example.auth_server.enums.Permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "permission_group_items")
@IdClass(PermissionGroupItemId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionGroupItem {

    @Id
    @Column(name = "group_name", length = 50)
    private String groupName;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "required_permission", length = 50)
    private Permission requiredPermission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_name", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PermissionGroup permissionGroup;
}
