package com.kompu.api.entity.user.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.user.exception.UserNotFoundException;
import com.kompu.api.entity.user.model.UserAccountModel;

public interface UserGateway {

    UserAccountModel create(UserAccountModel userAccountModel);

    UserAccountModel update(UserAccountModel userAccountModel);

    void delete(UUID id) throws UserNotFoundException;

    Optional<UserAccountModel> findById(UUID id);

    Optional<UserAccountModel> findByUsername(String username);

    Optional<UserAccountModel> findByEmail(String email);

    List<UserAccountModel> findByTenantId(UUID tenantId);

    List<UserAccountModel> findAll();

}
