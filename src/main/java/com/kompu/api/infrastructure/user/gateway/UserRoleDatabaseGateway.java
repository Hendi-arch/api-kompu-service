package com.kompu.api.infrastructure.user.gateway;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.kompu.api.entity.user.gateway.UserRoleGateway;
import com.kompu.api.entity.user.model.UserRoleModel;
import com.kompu.api.infrastructure.config.db.repository.UserRoleRepository;
import com.kompu.api.infrastructure.config.db.schema.UserRoleId;
import com.kompu.api.infrastructure.config.db.schema.UserRoleSchema;

@Component
public class UserRoleDatabaseGateway implements UserRoleGateway {

    private final UserRoleRepository repository;

    public UserRoleDatabaseGateway(UserRoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserRoleModel create(UserRoleModel userRoleModel) {
        UserRoleSchema schema = UserRoleSchema.builder()
                .userId(userRoleModel.getUserId())
                .roleId(userRoleModel.getRoleId())
                .assignedAt(userRoleModel.getAssignedAt() != null ? userRoleModel.getAssignedAt() : LocalDateTime.now())
                .build();
        return repository.save(schema).toUserRoleModel();
    }

    @Override
    public void delete(UUID userId, UUID roleId) {
        repository.deleteById(new UserRoleId(userId, roleId));
    }

    @Override
    public List<UserRoleModel> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(UserRoleSchema::toUserRoleModel)
                .toList();
    }

    @Override
    public List<UserRoleModel> findByRoleId(UUID roleId) {
        return repository.findByRoleId(roleId).stream()
                .map(UserRoleSchema::toUserRoleModel)
                .toList();
    }

    @Override
    public List<UserRoleModel> findAll() {
        return repository.findAll().stream()
                .map(UserRoleSchema::toUserRoleModel)
                .toList();
    }

    @Override
    public boolean hasRole(UUID userId, UUID roleId) {
        return repository.existsByUserIdAndRoleId(userId, roleId);
    }

}
