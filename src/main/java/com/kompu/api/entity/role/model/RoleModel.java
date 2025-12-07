package com.kompu.api.entity.role.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.kompu.api.entity.AbstractEntity;
import com.kompu.api.entity.permission.model.PermissionModel;

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
public class RoleModel extends AbstractEntity<UUID> {

    private UUID id;

    private UUID tenantId;

    private String name;

    private String description;

    private boolean isSystem;

    @Builder.Default
    private Set<PermissionModel> permissions = new HashSet<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
