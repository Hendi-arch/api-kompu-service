package com.kompu.api.infrastructure.auth.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kompu.api.entity.user.model.UserAccountModel;
import com.kompu.api.entity.usertoken.model.RefreshTokenModel;
import com.kompu.api.entity.usertoken.model.UserSessionModel;
import com.kompu.api.infrastructure.auth.dto.AuthTokenResponse;
import com.kompu.api.infrastructure.config.web.response.WebHttpResponse;
import com.kompu.api.usecase.auth.dto.IAuthTokenResponse;
import com.kompu.api.usecase.auth.dto.IChangePasswordRequest;
import com.kompu.api.usecase.auth.dto.IRefreshTokenRequest;
import com.kompu.api.usecase.auth.dto.ISignInRequest;
import com.kompu.api.usecase.auth.dto.ISignUpRequest;
import com.kompu.api.usecase.user.ChangePasswordUseCase;
import com.kompu.api.usecase.user.CreateUserUseCase;
import com.kompu.api.usecase.user.GetUserUseCase;
import com.kompu.api.usecase.user.ValidateUserCredentialsUseCase;
import com.kompu.api.usecase.usertoken.CreateUserSessionUseCase;
import com.kompu.api.usecase.usertoken.GenerateAccessTokenUseCase;
import com.kompu.api.usecase.usertoken.GenerateRefreshTokenUseCase;
import com.kompu.api.usecase.usertoken.ValidateRefreshTokenUseCase;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

        private final CreateUserUseCase createUserUseCase;
        private final ValidateUserCredentialsUseCase validateUserCredentialsUseCase;
        private final ChangePasswordUseCase changePasswordUseCase;
        private final GetUserUseCase getUserUseCase;
        private final GenerateAccessTokenUseCase generateAccessTokenUseCase;
        private final GenerateRefreshTokenUseCase generateRefreshTokenUseCase;
        private final ValidateRefreshTokenUseCase validateRefreshTokenUseCase;
        private final CreateUserSessionUseCase createUserSessionUseCase;

        public AuthController(
                        CreateUserUseCase createUserUseCase,
                        ValidateUserCredentialsUseCase validateUserCredentialsUseCase,
                        ChangePasswordUseCase changePasswordUseCase,
                        GetUserUseCase getUserUseCase,
                        GenerateAccessTokenUseCase generateAccessTokenUseCase,
                        GenerateRefreshTokenUseCase generateRefreshTokenUseCase,
                        ValidateRefreshTokenUseCase validateRefreshTokenUseCase,
                        CreateUserSessionUseCase createUserSessionUseCase) {
                this.createUserUseCase = createUserUseCase;
                this.validateUserCredentialsUseCase = validateUserCredentialsUseCase;
                this.changePasswordUseCase = changePasswordUseCase;
                this.getUserUseCase = getUserUseCase;
                this.generateAccessTokenUseCase = generateAccessTokenUseCase;
                this.generateRefreshTokenUseCase = generateRefreshTokenUseCase;
                this.validateRefreshTokenUseCase = validateRefreshTokenUseCase;
                this.createUserSessionUseCase = createUserSessionUseCase;
        }

        /**
         * Sign up endpoint - creates a new user account.
         * POST /api/v1/auth/signup
         */
        @PostMapping("/signup")
        public ResponseEntity<WebHttpResponse<IAuthTokenResponse>> signUp(
                        @RequestBody ISignUpRequest request,
                        HttpServletRequest httpRequest) {
                log.info("Sign up request for username: {}", request.username());

                // Validate passwords match
                if (!request.password().equals(request.confirmPassword())) {
                        log.warn("Password confirmation mismatch for signup");
                        return ResponseEntity.badRequest()
                                        .body(new WebHttpResponse<>());
                }

                // Create user (using default tenant for now - can be enhanced for multi-tenant
                // signup)
                UUID defaultTenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
                UserAccountModel newUser = createUserUseCase.createUser(
                                request.username(),
                                request.email(),
                                request.password(),
                                request.fullName(),
                                defaultTenantId);

                // Create session
                UserSessionModel session = createUserSessionUseCase.createSession(
                                newUser,
                                getClientIpAddress(httpRequest),
                                httpRequest.getHeader("User-Agent"));

                // Generate tokens
                String accessToken = generateAccessTokenUseCase.generateAccessToken(newUser);
                String refreshToken = generateRefreshTokenUseCase.generateAndStoreRefreshToken(newUser,
                                session.getId());

                IAuthTokenResponse response = buildAuthTokenResponse(accessToken, refreshToken, newUser);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(WebHttpResponse.created(response));
        }

        /**
         * Sign in endpoint - authenticates user and returns tokens.
         * POST /api/v1/auth/signin
         */
        @PostMapping("/signin")
        public ResponseEntity<WebHttpResponse<IAuthTokenResponse>> signIn(
                        @RequestBody ISignInRequest request,
                        HttpServletRequest httpRequest) {
                log.info("Sign in request for username: {}", request.username());

                // Validate credentials
                UserAccountModel user = validateUserCredentialsUseCase.validateCredentials(
                                request.username(),
                                request.password());

                // Create session
                UserSessionModel session = createUserSessionUseCase.createSession(
                                user,
                                getClientIpAddress(httpRequest),
                                httpRequest.getHeader("User-Agent"));

                // Generate tokens
                String accessToken = generateAccessTokenUseCase.generateAccessToken(user);
                String refreshToken = generateRefreshTokenUseCase.generateAndStoreRefreshToken(user, session.getId());

                IAuthTokenResponse response = buildAuthTokenResponse(accessToken, refreshToken, user);

                return ResponseEntity.ok(
                                WebHttpResponse.ok(response));
        }

        /**
         * Refresh token endpoint - generates new access token using refresh token.
         * POST /api/v1/auth/refresh
         */
        @PostMapping("/refresh")
        public ResponseEntity<WebHttpResponse<IAuthTokenResponse>> refreshToken(
                        @RequestBody IRefreshTokenRequest request) {
                log.info("Refresh token request");

                // Validate refresh token
                RefreshTokenModel refreshToken = validateRefreshTokenUseCase.validateRefreshToken(
                                request.refreshToken());

                // Get user
                UserAccountModel user = getUserUseCase.findById(refreshToken.getUserId());

                // Generate new access token
                String newAccessToken = generateAccessTokenUseCase.generateAccessToken(user);

                IAuthTokenResponse response = buildAuthTokenResponse(newAccessToken, request.refreshToken(), user);

                return ResponseEntity.ok(
                                WebHttpResponse.ok(response));
        }

        /**
         * Change password endpoint - changes user's password.
         * PUT /api/v1/auth/change-password
         */
        @PutMapping("/change-password")
        public ResponseEntity<WebHttpResponse<String>> changePassword(
                        @RequestBody IChangePasswordRequest request,
                        @RequestHeader("Authorization") String authHeader) {
                log.info("Change password request");

                // Validate new passwords match
                if (!request.newPassword().equals(request.confirmPassword())) {
                        log.warn("New password confirmation mismatch");
                        return ResponseEntity.badRequest()
                                        .body(new WebHttpResponse<>());
                }

                // Extract username from auth header (would normally come from SecurityContext)
                String username = extractUsernameFromAuth(authHeader);

                // Change password
                changePasswordUseCase.changePassword(
                                username,
                                request.oldPassword(),
                                request.newPassword());

                return ResponseEntity.ok(
                                WebHttpResponse.ok("Password changed successfully"));
        }

        // ==================== Helper Methods ====================

        private IAuthTokenResponse buildAuthTokenResponse(String accessToken, String refreshToken,
                        UserAccountModel user) {
                AuthTokenResponse.UserAuthResponse userAuthResponse = new AuthTokenResponse.UserAuthResponse(
                                user.getId().toString(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getFullName(),
                                user.getPhone(),
                                user.getAvatarUrl(),
                                user.isEmailVerified(),
                                user.getCreatedAt());

                return new AuthTokenResponse(
                                accessToken,
                                refreshToken,
                                "Bearer",
                                7 * 24 * 60 * 60L, // 7 days in seconds
                                userAuthResponse);
        }

        private String getClientIpAddress(HttpServletRequest request) {
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                        return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
        }

        private String extractUsernameFromAuth(String authHeader) {
                // This is a placeholder - in real implementation, extract from SecurityContext
                // For now, we'll need to pass the username through a custom header or request
                // body
                return "current_user";
        }

}
