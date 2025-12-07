package com.kompu.api.entity.role.gateway;

import java.util.List;
import java.util.UUID;

import com.kompu.api.entity.role.model.RolePermissionModel;

public interface RolePermissionGateway {

    RolePermissionModel create(RolePermissionModel rolePermissionModel);

    void delete(UUID roleId, UUID permissionId);

    List<RolePermissionModel> findByRoleId(UUID roleId);

    List<RolePermissionModel> findByPermissionId(UUID permissionId);

    List<RolePermissionModel> findAll();

    boolean hasPermission(UUID roleId, UUID permissionId);

}
