package com.kompu.api.entity.permission.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.permission.exception.PermissionNotFoundException;
import com.kompu.api.entity.permission.model.PermissionModel;

public interface PermissionGateway {

    PermissionModel create(PermissionModel permissionModel);

    PermissionModel update(PermissionModel permissionModel);

    void delete(UUID id) throws PermissionNotFoundException;

    Optional<PermissionModel> findById(UUID id);

    Optional<PermissionModel> findByCode(String code);

    List<PermissionModel> findAll();

}
