package com.kompu.api.infrastructure.role.gateway;

import java.util.List;
import java.util.UUID;

import com.kompu.api.entity.role.gateway.RolePermissionGateway;
import com.kompu.api.entity.role.model.RolePermissionModel;
import com.kompu.api.infrastructure.config.db.repository.RolePermissionRepository;
import com.kompu.api.infrastructure.config.db.schema.RolePermissionSchema;

public class RolePermissionDatabaseGateway implements RolePermissionGateway {

    private final RolePermissionRepository repository;

    public RolePermissionDatabaseGateway(RolePermissionRepository repository) {
        this.repository = repository;
    }

    @Override
    public RolePermissionModel create(RolePermissionModel rolePermissionModel) {
        return repository.save(new RolePermissionSchema(rolePermissionModel)).toRolePermissionModel();
    }

    @Override
    public void delete(UUID roleId, UUID permissionId) {
        repository.deleteById(new com.kompu.api.infrastructure.config.db.schema.RolePermissionId(roleId, permissionId));
    }

    @Override
    public RolePermissionModel findByRoleId(UUID roleId) {
        return repository.findByRoleId(roleId).toRolePermissionModel();
    }

    @Override
    public List<RolePermissionModel> findByPermissionId(UUID permissionId) {
        return repository.findByPermissionId(permissionId).stream()
                .map(RolePermissionSchema::toRolePermissionModel)
                .toList();
    }

    @Override
    public List<RolePermissionModel> findAll() {
        return repository.findAll().stream()
                .map(RolePermissionSchema::toRolePermissionModel)
                .toList();
    }

    @Override
    public boolean hasPermission(UUID roleId, UUID permissionId) {
        return repository.existsByRoleIdAndPermissionId(roleId, permissionId);
    }

}
