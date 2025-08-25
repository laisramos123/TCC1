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

import com.example.auth_server.dto.ConsentPermissionId;
import com.example.auth_server.enums.Permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "consent_permissions")
@IdClass(ConsentPermissionId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentPermission {

    @Id
    @Column(name = "consent_id", length = 256)
    private String consentId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", length = 50)
    private Permission permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Consent consent;
}
