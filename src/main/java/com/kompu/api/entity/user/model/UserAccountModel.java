package com.kompu.api.entity.user.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.kompu.api.entity.AbstractEntity;
import com.kompu.api.entity.role.model.RoleModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserAccountModel extends AbstractEntity<UUID> {

    private UUID id;

    private UUID tenantId;

    private String email;

    private String passwordHash;

    private String fullName;

    private String phone;

    private String avatarUrl;

    private boolean isActive;

    private boolean isEmailVerified;

    private boolean isSystem;

    @Builder.Default
    private Set<RoleModel> roles = new HashSet<>();

    private String createdBy;

    private String updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

}
