package com.kompu.api.entity.usertoken.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.usertoken.exception.RefreshTokenNotFoundException;
import com.kompu.api.entity.usertoken.model.RefreshTokenModel;

public interface RefreshTokenGateway {

    RefreshTokenModel create(RefreshTokenModel refreshTokenModel);

    void delete(UUID id) throws RefreshTokenNotFoundException;

    Optional<RefreshTokenModel> findById(UUID id);

    List<RefreshTokenModel> findByUserId(UUID userId);

    List<RefreshTokenModel> findBySessionId(UUID sessionId);

    Optional<RefreshTokenModel> findByTokenHash(String tokenHash);

    void revokeToken(UUID tokenId);

    void revokeAllTokensByUserId(UUID userId);

}
