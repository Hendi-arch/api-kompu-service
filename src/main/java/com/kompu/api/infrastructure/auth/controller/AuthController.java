package com.kompu.api.infrastructure.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kompu.api.infrastructure.auth.dto.AuthTokenResponse;
import com.kompu.api.infrastructure.auth.dto.SignUpRequest;
import com.kompu.api.infrastructure.config.web.response.WebHttpResponse;

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

        public AuthController() {

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
                log.info("Sign up request for username: {}", request.username());
                // TODO: Implement sign-up logic
                throw new UnsupportedOperationException("Sign-up not implemented yet");
        }

}
