package com.kompu.api.entity.usertoken.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.usertoken.exception.UserSessionNotFoundException;
import com.kompu.api.entity.usertoken.model.UserSessionModel;

public interface UserSessionGateway {

    UserSessionModel create(UserSessionModel userSessionModel);

    UserSessionModel update(UserSessionModel userSessionModel);

    void delete(UUID id) throws UserSessionNotFoundException;

    Optional<UserSessionModel> findById(UUID id);

    List<UserSessionModel> findByUserId(UUID userId);

    List<UserSessionModel> findActiveSessionsByUserId(UUID userId);

    List<UserSessionModel> findByTenantId(UUID tenantId);

    void deactivateSession(UUID sessionId);

}
