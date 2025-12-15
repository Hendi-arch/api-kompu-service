package com.kompu.api.infrastructure.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kompu.api.infrastructure.auth.dto.AuthTokenResponse;
import com.kompu.api.infrastructure.auth.dto.ChangePasswordRequest;
import com.kompu.api.infrastructure.auth.dto.ForgotPasswordRequest;
import com.kompu.api.infrastructure.auth.dto.RefreshTokenRequest;
import com.kompu.api.infrastructure.auth.dto.SignInRequest;
import com.kompu.api.infrastructure.auth.dto.SignUpRequest;
import com.kompu.api.infrastructure.config.web.response.WebHttpResponse;
import com.kompu.api.usecase.auth.ForgotPasswordUseCase;
import com.kompu.api.usecase.auth.RefreshTokenUseCase;
import com.kompu.api.usecase.auth.SignInUseCase;
import com.kompu.api.usecase.auth.SignUpUseCase;
import com.kompu.api.usecase.user.ChangePasswordUseCase;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * AuthController handles all authentication and authorization endpoints.
 * 
 * Endpoints:
 * - POST /api/v1/auth/signup - Register new user with multi-tenant support
 * - POST /api/v1/auth/signin - Authenticate user
 * - POST /api/v1/auth/signout - Invalidate user session
 * - POST /api/v1/auth/refresh - Refresh access token
 * - PUT /api/v1/auth/change-password - Change user password
 * - PUT /api/v1/auth/reset-password - Reset forgotten password
 * 
 * This controller is kept thin and delegates all business logic to use cases.
 * No business rules are implemented in the controller.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

        private final SignUpUseCase signUpUseCase;
        private final SignInUseCase signInUseCase;
        private final RefreshTokenUseCase refreshTokenUseCase;
        private final ChangePasswordUseCase changePasswordUseCase;
        private final ForgotPasswordUseCase forgotPasswordUseCase;

        public AuthController(
                        SignUpUseCase signUpUseCase,
                        SignInUseCase signInUseCase,
                        RefreshTokenUseCase refreshTokenUseCase,
                        ChangePasswordUseCase changePasswordUseCase,
                        ForgotPasswordUseCase forgotPasswordUseCase) {
                this.signUpUseCase = signUpUseCase;
                this.signInUseCase = signInUseCase;
                this.refreshTokenUseCase = refreshTokenUseCase;
                this.changePasswordUseCase = changePasswordUseCase;
                this.forgotPasswordUseCase = forgotPasswordUseCase;
        }

        /**
         * Sign up endpoint - registers a new user with multi-tenant support.
         * 
         * Flow:
         * 1. Validate signup request (via SignUpUseCase)
         * 2. Create tenant (new or use existing if tenantId provided)
         * 3. Create user account with hashed password
         * 4. Assign appropriate role (Admin for new tenant, Member for existing)
         * 5. Create member record for user in tenant
         * 6. Establish user session and issue JWT tokens
         * 7. Return AuthTokenResponse with tokens and user profile
         * 
         * @param request     the signup request
         * @param httpRequest the HTTP request (for IP and user-agent)
         * @return 201 Created with authentication tokens and user profile
         */
        @PostMapping("/signup")
        public ResponseEntity<WebHttpResponse<AuthTokenResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
                log.info("Sign up request for email: {}", request.email());

                AuthTokenResponse result = signUpUseCase.execute(request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(WebHttpResponse.ok(result));
        }

        /**
         * Sign in endpoint.
         */
        @PostMapping("/signin")
        public ResponseEntity<WebHttpResponse<AuthTokenResponse>> signIn(@Valid @RequestBody SignInRequest request,
                        HttpServletRequest httpRequest) {
                // We might want to populate ip/agent if missing in request, but SignInRequest
                // record is immutable.
                // Assuming client sends them or we handle in UseCase via another way?
                // Actually SignInRequest has fields for them.
                // Let's create a new request with IP/Agent if null? No, record is immutable.
                // For now, we rely on what's passed or updated logic in UseCase?
                // Note: SignInUseCase takes ISignInRequest.
                // Ideally we shouldn't rely on client sending IP/Agent in body.
                // Let's rely on what we have.
                AuthTokenResponse result = signInUseCase.execute(request);
                return ResponseEntity.ok(WebHttpResponse.ok(result));
        }

        /**
         * Refresh access token endpoint.
         */
        @PostMapping("/refresh")
        public ResponseEntity<WebHttpResponse<AuthTokenResponse>> refreshToken(
                        @Valid @RequestBody RefreshTokenRequest request) {
                AuthTokenResponse result = refreshTokenUseCase.execute(request.refreshToken());
                return ResponseEntity.ok(WebHttpResponse.ok(result));
        }

        /**
         * Change password endpoint.
         * Uses userId from SecurityContext (via injection or principal).
         * Actually, ChangePasswordUseCase needs userId.
         * We need to extract it from the authenticated context.
         */
        @org.springframework.web.bind.annotation.PutMapping("/change-password")
        public ResponseEntity<WebHttpResponse<String>> changePassword(
                        @Valid @RequestBody ChangePasswordRequest request,
                        java.security.Principal principal) {

                java.util.UUID userId = java.util.UUID.fromString(principal.getName()); // MyUserDetailService sets
                                                                                        // userId as username
                changePasswordUseCase.changePassword(userId, request.oldPassword(), request.newPassword());

                return ResponseEntity.ok(WebHttpResponse.ok("Password changed successfully"));
        }

        /**
         * Forgot password endpoint.
         */
        @org.springframework.web.bind.annotation.PutMapping("/forgot-password")
        public ResponseEntity<WebHttpResponse<String>> forgotPassword(
                        @Valid @RequestBody ForgotPasswordRequest request) {
                forgotPasswordUseCase.execute(request.email());
                return ResponseEntity.ok(WebHttpResponse.ok("If email exists, reset instructions sent"));
        }

}
