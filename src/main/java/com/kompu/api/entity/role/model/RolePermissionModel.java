package com.kompu.api.entity.role.model;

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
public class RolePermissionModel extends AbstractEntity<String> {

    private UUID roleId;

    private UUID permissionId;

    private RoleModel role;

    private PermissionModel permission;

}
