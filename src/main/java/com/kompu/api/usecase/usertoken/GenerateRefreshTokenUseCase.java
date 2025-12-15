package com.kompu.api.usecase.usertoken;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import com.kompu.api.entity.usertoken.gateway.RefreshTokenGateway;
import com.kompu.api.entity.usertoken.model.RefreshTokenModel;
import com.kompu.api.entity.user.model.UserAccountModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateRefreshTokenUseCase {

    private final RefreshTokenGateway refreshTokenGateway;
    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 30;

    public GenerateRefreshTokenUseCase(RefreshTokenGateway refreshTokenGateway) {
        this.refreshTokenGateway = refreshTokenGateway;
    }

    /**
     * Generates and stores a new refresh token for the user.
     * The refresh token is hashed using Base64 for secure storage.
     *
     * @param userAccount the UserAccountModel
     * @param sessionId   the user session ID
     * @return the generated refresh token (raw, unhashed)
     */
    public String generateAndStoreRefreshToken(UserAccountModel userAccount, UUID sessionId) {
        log.info("Generating refresh token for user: {}", userAccount.getEmail());

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = Base64.getEncoder().encodeToString(rawToken.getBytes());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(REFRESH_TOKEN_EXPIRY_DAYS);

        RefreshTokenModel refreshToken = RefreshTokenModel.builder()
                .id(UUID.randomUUID())
                .userId(userAccount.getId())
                .sessionId(sessionId)
                .tokenHash(tokenHash)
                .createdAt(now)
                .expiresAt(expiresAt)
                .revokedAt(null)
                .build();

        refreshTokenGateway.create(refreshToken);
        log.info("Refresh token stored for user: {}", userAccount.getEmail());

        return rawToken;
    }

}
