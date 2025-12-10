package com.kompu.api.usecase.usertoken;

import java.util.Base64;

import com.kompu.api.entity.usertoken.exception.RefreshTokenNotFoundException;
import com.kompu.api.entity.usertoken.gateway.RefreshTokenGateway;
import com.kompu.api.entity.usertoken.model.RefreshTokenModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateRefreshTokenUseCase {

    private final RefreshTokenGateway refreshTokenGateway;

    public ValidateRefreshTokenUseCase(RefreshTokenGateway refreshTokenGateway) {
        this.refreshTokenGateway = refreshTokenGateway;
    }

    /**
     * Validates a refresh token and returns the RefreshTokenModel if valid.
     * Checks that token exists, is not revoked, and is not expired.
     *
     * @param rawToken the raw refresh token
     * @return the RefreshTokenModel if valid
     */
    public RefreshTokenModel validateRefreshToken(String rawToken) {
        log.info("Validating refresh token");

        String tokenHash = Base64.getEncoder().encodeToString(rawToken.getBytes());

        RefreshTokenModel refreshToken = refreshTokenGateway.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found");
                    return new RefreshTokenNotFoundException();
                });

        // Check if token is revoked
        if (refreshToken.getRevokedAt() != null) {
            log.warn("Refresh token has been revoked for user: {}", refreshToken.getUserId());
            throw new RefreshTokenNotFoundException();
        }

        // Check if token is expired
        if (java.time.LocalDateTime.now().isAfter(refreshToken.getExpiresAt())) {
            log.warn("Refresh token has expired for user: {}", refreshToken.getUserId());
            throw new RefreshTokenNotFoundException();
        }

        log.info("Refresh token validated successfully for user: {}", refreshToken.getUserId());
        return refreshToken;
    }

}
