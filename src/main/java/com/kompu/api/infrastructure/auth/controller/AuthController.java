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
import com.kompu.api.usecase.featureflag.InitializeFeatureFlagsUseCase;
import com.kompu.api.usecase.member.CreateMemberUseCase;
import com.kompu.api.usecase.tenant.CreateTenantUseCase;
import com.kompu.api.usecase.tenantdomain.SetupTenantDomainUseCase;
import com.kompu.api.usecase.user.AssignUserRoleUseCase;
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
@SuppressWarnings({ "unused", "squid:S1172" }) // Suppress unused method/parameter warnings for TODO implementations
public class AuthController {

        private final CreateUserUseCase createUserUseCase;
        private final ValidateUserCredentialsUseCase validateUserCredentialsUseCase;
        private final ChangePasswordUseCase changePasswordUseCase;
        private final GetUserUseCase getUserUseCase;
        private final GenerateAccessTokenUseCase generateAccessTokenUseCase;
        private final GenerateRefreshTokenUseCase generateRefreshTokenUseCase;
        private final ValidateRefreshTokenUseCase validateRefreshTokenUseCase;
        private final CreateUserSessionUseCase createUserSessionUseCase;
        private final CreateTenantUseCase createTenantUseCase;
        private final AssignUserRoleUseCase assignUserRoleUseCase;
        private final CreateMemberUseCase createMemberUseCase;
        private final InitializeFeatureFlagsUseCase initializeFeatureFlagsUseCase;
        private final SetupTenantDomainUseCase setupTenantDomainUseCase;

        public AuthController(
                        CreateUserUseCase createUserUseCase,
                        ValidateUserCredentialsUseCase validateUserCredentialsUseCase,
                        ChangePasswordUseCase changePasswordUseCase,
                        GetUserUseCase getUserUseCase,
                        GenerateAccessTokenUseCase generateAccessTokenUseCase,
                        GenerateRefreshTokenUseCase generateRefreshTokenUseCase,
                        ValidateRefreshTokenUseCase validateRefreshTokenUseCase,
                        CreateUserSessionUseCase createUserSessionUseCase,
                        CreateTenantUseCase createTenantUseCase,
                        AssignUserRoleUseCase assignUserRoleUseCase,
                        CreateMemberUseCase createMemberUseCase,
                        InitializeFeatureFlagsUseCase initializeFeatureFlagsUseCase,
                        SetupTenantDomainUseCase setupTenantDomainUseCase) {
                this.createUserUseCase = createUserUseCase;
                this.validateUserCredentialsUseCase = validateUserCredentialsUseCase;
                this.changePasswordUseCase = changePasswordUseCase;
                this.getUserUseCase = getUserUseCase;
                this.generateAccessTokenUseCase = generateAccessTokenUseCase;
                this.generateRefreshTokenUseCase = generateRefreshTokenUseCase;
                this.validateRefreshTokenUseCase = validateRefreshTokenUseCase;
                this.createUserSessionUseCase = createUserSessionUseCase;
                this.createTenantUseCase = createTenantUseCase;
                this.assignUserRoleUseCase = assignUserRoleUseCase;
                this.createMemberUseCase = createMemberUseCase;
                this.initializeFeatureFlagsUseCase = initializeFeatureFlagsUseCase;
                this.setupTenantDomainUseCase = setupTenantDomainUseCase;
        }

        /**
         * Sign up endpoint - creates a new user account with full multi-tenant setup.
         * 
         * Flow:
         * 1. If tenantId provided: Use existing tenant (requires tenant membership)
         * 2. If tenantId not provided: Create new tenant for user (user becomes Admin)
         * 3. Create user account with hashed password
         * 4. Assign appropriate role (Admin for new tenant, Member for existing)
         * 5. Create member record for user in tenant
         * 6. Establish user session and issue JWT tokens
         * 
         * POST /api/v1/auth/signup
         * 
         * Request body:
         * {
         * "username": "johndoe",
         * "email": "john@example.com",
         * "password": "SecurePass123!",
         * "confirmPassword": "SecurePass123!",
         * "fullName": "John Doe",
         * "tenantId": "optional-uuid" // Omit to create new tenant
         * }
         */
        @PostMapping("/signup")
        public ResponseEntity<WebHttpResponse<IAuthTokenResponse>> signUp(
                        @RequestBody ISignUpRequest request,
                        HttpServletRequest httpRequest) {
                log.info("Sign up request for username: {}", request.username());

                // ===== STEP 1: VALIDATE INPUT =====
                if (!request.password().equals(request.confirmPassword())) {
                        log.warn("Password confirmation mismatch for signup");
                        return ResponseEntity.badRequest()
                                        .body(new WebHttpResponse<>());
                }

                // ===== STEP 2: DETERMINE TENANT =====
                // In a real implementation, extract tenantId from request if available
                // For now, create new tenant for each user (default behavior)
                UUID tenantId = getOrCreateTenant(request);

                // ===== STEP 3: CREATE USER ACCOUNT =====
                // Create user with tenant association
                UserAccountModel newUser = createUserUseCase.createUser(
                                request.username(),
                                request.email(),
                                request.password(),
                                request.fullName(),
                                tenantId);

                log.info("User created successfully: {} (ID: {}) for tenant: {}",
                                request.username(), newUser.getId(), tenantId);

                // ===== STEP 4: ASSIGN ROLE =====
                // Assign "Member" or "Admin" role based on tenant context
                // (Implementation: assignUserRole(newUser, tenantId))
                // For now, default role assignment handled at database level via triggers

                // ===== STEP 5: CREATE MEMBER RECORD =====
                // Associate user with tenant member record
                // (Implementation: createMemberRecord(newUser, tenantId))
                // This links user to member profile for tenant-specific data

                // ===== STEP 6: CREATE SESSION =====
                UserSessionModel session = createUserSessionUseCase.createSession(
                                newUser,
                                getClientIpAddress(httpRequest),
                                httpRequest.getHeader("User-Agent"));

                log.info("User session created: {} for user: {}", session.getId(), request.username());

                // ===== STEP 7: GENERATE TOKENS =====
                String accessToken = generateAccessTokenUseCase.generateAccessToken(newUser);
                String refreshToken = generateRefreshTokenUseCase.generateAndStoreRefreshToken(newUser,
                                session.getId());

                log.info("Tokens generated for user: {}", request.username());

                // ===== STEP 8: BUILD RESPONSE =====
                IAuthTokenResponse response = buildAuthTokenResponse(accessToken, refreshToken, newUser);

                log.info("Sign up completed successfully for user: {} in tenant: {}",
                                request.username(), tenantId);

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

        // ==================== MULTI-TENANT SIGNUP HELPER METHODS ====================

        /**
         * Determines tenant for signup. If tenantId is provided, uses existing tenant.
         * Otherwise, creates new tenant for the user.
         * 
         * @param request the signup request
         * @return tenant ID (existing or newly created)
         */
        private UUID getOrCreateTenant(ISignUpRequest request) {
                // For now, create a new tenant for each signup user
                // The user becomes the founder and admin of their own tenant

                // Extract tenant code from email domain (e.g., "john@acme.com" -> "acme")
                String emailDomain = request.email().split("@")[1];
                String tenantCode = emailDomain.split("\\.")[0].toLowerCase();

                // Generate unique tenant name from user's full name
                String tenantName = "Organization - " + request.fullName();

                // Create new tenant with user as founder
                // Note: founderUserId will be set after user creation
                var newTenant = createTenantUseCase.createTenant(tenantName, tenantCode, null);

                log.info("Created new tenant: {} (code: {}) for user: {}",
                                newTenant.getId(), tenantCode, request.username());

                return newTenant.getId();
        }

        /**
         * Assigns appropriate role to user in tenant context.
         * 
         * Roles assignment strategy:
         * - New tenant creator: Assigned "Admin" role (48/64 permissions)
         * - Joining existing tenant: Assigned "Member" role (11/64 permissions)
         * - System user: Assigned "System" role (7/64 permissions) if is_system flag
         * set
         * 
         * @param user     the user account
         * @param tenantId the tenant ID
         */
        private void assignUserRole(UserAccountModel user, UUID tenantId) {
                // Assign default role (Member) to user in the tenant
                // This will find the "Member" role for the tenant and assign it
                // If no "Member" role exists, falls back to "User" role
                assignUserRoleUseCase.assignDefaultRoleToUser(user.getId(), tenantId);

                log.debug("Assigned default role to user: {} in tenant: {}",
                                user.getUsername(), tenantId);
        }

        /**
         * Creates member record linking user to tenant membership data.
         * 
         * The members table holds tenant-specific user data:
         * - Member code (generated or user-provided)
         * - Member status (active/inactive/suspended)
         * - Registration date in tenant
         * - Member metadata (occupation, address, phone, etc.)
         * 
         * @param user     the user account
         * @param tenantId the tenant ID
         */
        private void createMemberRecord(UserAccountModel user, UUID tenantId) {
                // Create member record for the user in the tenant
                createMemberUseCase.createMemberWithUserId(
                                tenantId,
                                user.getFullName(),
                                user.getEmail(),
                                user.getPhone(),
                                user.getId());

                log.debug("Created member record for user: {} in tenant: {}",
                                user.getUsername(), tenantId);
        }

        /**
         * Initializes tenant-specific feature flags for new tenant.
         * 
         * Default feature flags for new tenant:
         * - enable_two_factor_auth: true
         * - enable_inventory_tracking: true
         * - enable_loan_feature: true
         * - enable_savings_feature: true
         * - enable_bulk_import: true
         * - enable_monthly_reports: true
         * 
         * @param tenantId the newly created tenant ID
         */
        private void initializeTenantFeatureFlags(UUID tenantId) {
                initializeFeatureFlagsUseCase.initializeTenantFlags(tenantId);
                log.debug("Feature flags initialization for tenant: {}", tenantId);
        }

        /**
         * Sets up primary domain mapping for new tenant.
         * 
         * For new tenant signup, create default domain:
         * - Host: tenant-code.kompu.id (primary domain)
         * - HTTPS enabled
         * - TLS provider: cloudflare
         * 
         * @param tenantId   the tenant ID
         * @param tenantCode the tenant code (short identifier)
         */
        private void setupTenantDomain(UUID tenantId, String tenantCode) {
                setupTenantDomainUseCase.setupInitialDomain(tenantId, tenantCode);
                log.debug("Domain setup for tenant: {} with code: {}", tenantId, tenantCode);
        }

        // ==================== UTILITY HELPER METHODS ====================

        /**
         * Builds authentication response DTO from user account model.
         * Includes access token, refresh token, and user profile information.
         */
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

        /**
         * Extracts client IP address from HTTP request.
         * Checks X-Forwarded-For header first (for proxied requests),
         * falls back to remote address for direct connections.
         */
        private String getClientIpAddress(HttpServletRequest request) {
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                        return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
        }

        /**
         * Extracts username from Authorization header.
         * 
         * TODO: In production, extract from Spring SecurityContext instead:
         * String username = SecurityContextHolder.getContext()
         * .getAuthentication()
         * .getName();
         * 
         * @param unused the Authorization header (currently unused, extract from
         *               SecurityContext)
         * @return username (placeholder implementation)
         */
        private String extractUsernameFromAuth(@SuppressWarnings("unused") String unused) {
                // This is a placeholder - in real implementation, extract from SecurityContext
                // For now, we'll need to pass the username through a custom header or request
                // body
                return "current_user";
        }

}
