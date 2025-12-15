package com.kompu.api.usecase.auth;

import java.time.LocalDateTime;

import com.kompu.api.entity.system.gateway.LoginLogGateway;
import com.kompu.api.entity.system.model.LoginLogModel;
import com.kompu.api.entity.user.model.UserAccountModel;
import com.kompu.api.entity.usertoken.model.UserSessionModel;
import com.kompu.api.infrastructure.auth.dto.AuthTokenResponse;
import com.kompu.api.infrastructure.auth.dto.AuthTokenResponse.UserAuthResponse;
import com.kompu.api.usecase.auth.dto.ISignInRequest;
import com.kompu.api.usecase.user.ValidateUserCredentialsUseCase;
import com.kompu.api.usecase.usertoken.CreateUserSessionUseCase;
import com.kompu.api.usecase.usertoken.GenerateAccessTokenUseCase;
import com.kompu.api.usecase.usertoken.GenerateRefreshTokenUseCase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SignInUseCase {

    private final ValidateUserCredentialsUseCase validateUserCredentialsUseCase;
    private final CreateUserSessionUseCase createUserSessionUseCase;
    private final GenerateAccessTokenUseCase generateAccessTokenUseCase;
    private final GenerateRefreshTokenUseCase generateRefreshTokenUseCase;
    private final LoginLogGateway loginLogGateway;
    private static final long JWT_VALIDITY_SECONDS = 604800; // 7 days

    public SignInUseCase(
            ValidateUserCredentialsUseCase validateUserCredentialsUseCase,
            CreateUserSessionUseCase createUserSessionUseCase,
            GenerateAccessTokenUseCase generateAccessTokenUseCase,
            GenerateRefreshTokenUseCase generateRefreshTokenUseCase,
            LoginLogGateway loginLogGateway) {
        this.validateUserCredentialsUseCase = validateUserCredentialsUseCase;
        this.createUserSessionUseCase = createUserSessionUseCase;
        this.generateAccessTokenUseCase = generateAccessTokenUseCase;
        this.generateRefreshTokenUseCase = generateRefreshTokenUseCase;
        this.loginLogGateway = loginLogGateway;
    }

    public AuthTokenResponse execute(ISignInRequest request) {
        log.info("Attempting sign in for email: {}", request.email());

        try {
            // 1. Validate Credentials
            UserAccountModel user = validateUserCredentialsUseCase.validateCredentials(
                    request.email(), request.password());

            // 2. Create Session
            UserSessionModel session = createUserSessionUseCase.createSession(
                    user,
                    request.clientIpAddress(),
                    request.userAgent());

            // 3. Generate Tokens
            String accessToken = generateAccessTokenUseCase.generateAccessToken(user);
            String refreshToken = generateRefreshTokenUseCase.generateAndStoreRefreshToken(user, session.getId());

            // 4. Log Success
            logLoginAttempt(request, user, "success");

            return new AuthTokenResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    JWT_VALIDITY_SECONDS,
                    buildUserAuthResponse(user));

        } catch (Exception e) {
            log.warn("Sign in failed for email: {}", request.email(), e);
            // Log Failure (if user found, ideally. For now just based on request if
            // possible, but we might not have ID)
            // We can only log detailed info if we resolved the user.
            // For now, logging general failure in application log.
            // In a real system we might lookup user by email to get IDs for the log even on
            // password failure.
            throw e;
        }
    }

    private void logLoginAttempt(ISignInRequest request, UserAccountModel user, String result) {
        loginLogGateway.save(LoginLogModel.builder()
                .tenantId(user.getTenantId())
                .userId(user.getId())
                .email(user.getEmail())
                .ip(request.clientIpAddress())
                .userAgent(request.userAgent())
                .result(result)
                .createdAt(LocalDateTime.now())
                .build());
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
