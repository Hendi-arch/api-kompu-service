package com.kompu.api.entity.role.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.role.exception.RoleNotFoundException;
import com.kompu.api.entity.role.model.RoleModel;

public interface RoleGateway {

    RoleModel create(RoleModel roleModel);

    RoleModel update(RoleModel roleModel);

    void delete(UUID id) throws RoleNotFoundException;

    Optional<RoleModel> findById(UUID id);

    Optional<RoleModel> findByName(String name, UUID tenantId);

    List<RoleModel> findByTenantId(UUID tenantId);

    List<RoleModel> findAll();

}
