package com.kompu.api.infrastructure.user.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.user.gateway.UserGateway;
import com.kompu.api.entity.user.model.UserAccountModel;
import com.kompu.api.infrastructure.config.db.repository.UserRepository;
import com.kompu.api.infrastructure.config.db.schema.UserSchema;

public class UserDatabaseGateway implements UserGateway {

    private final UserRepository repository;

    public UserDatabaseGateway(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserAccountModel create(UserAccountModel userAccountModel) {
        return repository.save(new UserSchema(userAccountModel)).toUserAccountModel();
    }

    @Override
    public UserAccountModel update(UserAccountModel userAccountModel) {
        return repository.save(new UserSchema(userAccountModel)).toUserAccountModel();
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<UserAccountModel> findById(UUID id) {
        return repository.findByIdWithRolesAndPermissions(id).map(UserSchema::toUserAccountModel);
    }

    @Override
    public Optional<UserAccountModel> findByEmail(String email) {
        return repository.findByEmailWithRolesAndPermissions(email).map(UserSchema::toUserAccountModel);
    }

    @Override
    public List<UserAccountModel> findByTenantId(UUID tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(UserSchema::toUserAccountModel)
                .toList();
    }

    @Override
    public List<UserAccountModel> findAll() {
        return repository.findAll().stream()
                .map(UserSchema::toUserAccountModel)
                .toList();
    }

}
