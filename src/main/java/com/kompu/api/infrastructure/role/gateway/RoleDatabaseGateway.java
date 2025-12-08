package com.kompu.api.infrastructure.role.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.role.exception.RoleNotFoundException;
import com.kompu.api.entity.role.gateway.RoleGateway;
import com.kompu.api.entity.role.model.RoleModel;
import com.kompu.api.infrastructure.config.db.repository.RoleRepository;
import com.kompu.api.infrastructure.config.db.schema.RoleSchema;

public class RoleDatabaseGateway implements RoleGateway {

    private final RoleRepository repository;

    public RoleDatabaseGateway(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public RoleModel create(RoleModel roleModel) {
        return repository.save(new RoleSchema(roleModel)).toRoleModel();
    }

    @Override
    public RoleModel update(RoleModel roleModel) {
        return repository.save(new RoleSchema(roleModel)).toRoleModel();
    }

    @Override
    public void delete(UUID id) throws RoleNotFoundException {
        if (!repository.existsById(id)) {
            throw new RoleNotFoundException();
        }
        repository.deleteById(id);
    }

    @Override
    public Optional<RoleModel> findById(UUID id) {
        return repository.findById(id).map(RoleSchema::toRoleModel);
    }

    @Override
    public Optional<RoleModel> findByName(String name, UUID tenantId) {
        return repository.findByNameAndTenantId(name, tenantId).map(RoleSchema::toRoleModel);
    }

    @Override
    public List<RoleModel> findByTenantId(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(RoleSchema::toRoleModel)
                .toList();
    }

    @Override
    public List<RoleModel> findAll() {
        return repository.findAll().stream()
                .map(RoleSchema::toRoleModel)
                .toList();
    }

}
