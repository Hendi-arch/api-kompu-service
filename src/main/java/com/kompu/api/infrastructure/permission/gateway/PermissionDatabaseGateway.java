package com.kompu.api.infrastructure.permission.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.kompu.api.entity.permission.exception.PermissionNotFoundException;
import com.kompu.api.entity.permission.gateway.PermissionGateway;
import com.kompu.api.entity.permission.model.PermissionModel;
import com.kompu.api.infrastructure.config.db.repository.PermissionRepository;
import com.kompu.api.infrastructure.config.db.schema.PermissionSchema;

@Component
public class PermissionDatabaseGateway implements PermissionGateway {

    private final PermissionRepository repository;

    public PermissionDatabaseGateway(PermissionRepository repository) {
        this.repository = repository;
    }

    @Override
    public PermissionModel create(PermissionModel permissionModel) {
        return repository.save(new PermissionSchema(permissionModel)).toPermissionModel();
    }

    @Override
    public PermissionModel update(PermissionModel permissionModel) {
        return repository.save(new PermissionSchema(permissionModel)).toPermissionModel();
    }

    @Override
    public void delete(UUID id) throws PermissionNotFoundException {
        if (!repository.existsById(id)) {
            throw new PermissionNotFoundException();
        }
        repository.deleteById(id);
    }

    @Override
    public Optional<PermissionModel> findById(UUID id) {
        return repository.findById(id).map(PermissionSchema::toPermissionModel);
    }

    @Override
    public Optional<PermissionModel> findByCode(String code) {
        return repository.findByCode(code).map(PermissionSchema::toPermissionModel);
    }

    @Override
    public List<PermissionModel> findAll() {
        return repository.findAll().stream()
                .map(PermissionSchema::toPermissionModel)
                .toList();
    }

}
