package com.kompu.api.entity.user.gateway;

import java.util.List;
import java.util.UUID;

import com.kompu.api.entity.user.model.UserRoleModel;

public interface UserRoleGateway {

    UserRoleModel create(UserRoleModel userRoleModel);

    void delete(UUID userId, UUID roleId);

    List<UserRoleModel> findByUserId(UUID userId);

    List<UserRoleModel> findByRoleId(UUID roleId);

    List<UserRoleModel> findAll();

    boolean hasRole(UUID userId, UUID roleId);

}
