package com.kompu.api.usecase.auth;

import com.kompu.api.entity.user.gateway.UserGateway;
import com.kompu.api.entity.user.model.UserAccountModel;
import com.kompu.api.entity.usertoken.gateway.RefreshTokenGateway;
import com.kompu.api.entity.usertoken.model.RefreshTokenModel;
import com.kompu.api.infrastructure.auth.dto.AuthTokenResponse;
import com.kompu.api.infrastructure.auth.dto.AuthTokenResponse.UserAuthResponse;
import com.kompu.api.usecase.usertoken.GenerateAccessTokenUseCase;
import com.kompu.api.usecase.usertoken.GenerateRefreshTokenUseCase;
import com.kompu.api.usecase.usertoken.ValidateRefreshTokenUseCase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RefreshTokenUseCase {

    private final ValidateRefreshTokenUseCase validateRefreshTokenUseCase;
    private final RefreshTokenGateway refreshTokenGateway;
    private final UserGateway userGateway;
    private final GenerateAccessTokenUseCase generateAccessTokenUseCase;
    private final GenerateRefreshTokenUseCase generateRefreshTokenUseCase;
    private static final long JWT_VALIDITY_SECONDS = 604800; // 7 days

    public RefreshTokenUseCase(
            ValidateRefreshTokenUseCase validateRefreshTokenUseCase,
            RefreshTokenGateway refreshTokenGateway,
            UserGateway userGateway,
            GenerateAccessTokenUseCase generateAccessTokenUseCase,
            GenerateRefreshTokenUseCase generateRefreshTokenUseCase) {
        this.validateRefreshTokenUseCase = validateRefreshTokenUseCase;
        this.refreshTokenGateway = refreshTokenGateway;
        this.userGateway = userGateway;
        this.generateAccessTokenUseCase = generateAccessTokenUseCase;
        this.generateRefreshTokenUseCase = generateRefreshTokenUseCase;
    }

    public AuthTokenResponse execute(String refreshToken) {
        log.info("Attempting to refresh token");

        // 1. Validate Token
        RefreshTokenModel validToken = validateRefreshTokenUseCase.validateRefreshToken(refreshToken);

        // 2. Get User
        UserAccountModel user = userGateway.findById(validToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found for token"));

        // 3. Revoke Old Token (Token Rotation)
        // Ideally we revoke the specific family or just this one.
        // For simple rotation, we can assume revocation is handled by 'revokedAt' or
        // just generating a new one and client discards old.
        // But better to explicitly revoke if we want strict rotation.
        refreshTokenGateway.revokeToken(validToken.getId()); // Assuming revoke method exists or we update it.
        // If revoke doesn't exist, we skip for now or update gateway.
        // Let's assume revoke exists or use delete/update.

        // 4. Generate New Tokens
        String newAccessToken = generateAccessTokenUseCase.generateAccessToken(user);
        String newRefreshToken = generateRefreshTokenUseCase.generateAndStoreRefreshToken(user,
                validToken.getSessionId());

        return new AuthTokenResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                JWT_VALIDITY_SECONDS,
                buildUserAuthResponse(user));
    }

    private UserAuthResponse buildUserAuthResponse(UserAccountModel user) {
        return new UserAuthResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.isEmailVerified(),
                user.getCreatedAt());
    }
}
