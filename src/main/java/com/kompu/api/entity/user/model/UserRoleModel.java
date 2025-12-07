package com.kompu.api.entity.user.model;

import java.time.LocalDateTime;
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
public class UserRoleModel extends AbstractEntity<String> {

    private UUID userId;

    private UUID roleId;

    private UserAccountModel user;

    private RoleModel role;

    private LocalDateTime assignedAt;

}
