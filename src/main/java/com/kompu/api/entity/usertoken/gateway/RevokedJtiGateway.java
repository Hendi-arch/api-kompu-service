package com.kompu.api.entity.usertoken.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.usertoken.exception.RevokedJtiException;
import com.kompu.api.entity.usertoken.model.RevokedJtiModel;

public interface RevokedJtiGateway {

    RevokedJtiModel create(RevokedJtiModel revokedJtiModel);

    void delete(UUID jti);

    Optional<RevokedJtiModel> findByJti(UUID jti);

    List<RevokedJtiModel> findByUserId(UUID userId);

    List<RevokedJtiModel> findAll();

    boolean isRevoked(UUID jti) throws RevokedJtiException;

}
