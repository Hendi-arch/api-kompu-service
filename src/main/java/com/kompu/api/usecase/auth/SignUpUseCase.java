package com.kompu.api.usecase.auth;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.kompu.api.entity.member.gateway.MemberGateway;
import com.kompu.api.entity.member.model.MemberModel;
import com.kompu.api.entity.role.exception.RoleNotFoundException;
import com.kompu.api.entity.role.gateway.RoleGateway;
import com.kompu.api.entity.role.model.RoleModel;
import com.kompu.api.entity.tenant.gateway.TenantGateway;
import com.kompu.api.entity.tenant.model.TenantModel;
import com.kompu.api.entity.tenantdomain.gateway.TenantDomainGateway;
import com.kompu.api.entity.tenantdomain.model.TenantDomainModel;
import com.kompu.api.entity.user.gateway.UserGateway;
import com.kompu.api.entity.user.gateway.UserRoleGateway;
import com.kompu.api.entity.user.model.UserAccountModel;
import com.kompu.api.entity.user.model.UserRoleModel;
import com.kompu.api.entity.usertoken.gateway.RefreshTokenGateway;
import com.kompu.api.entity.usertoken.gateway.UserSessionGateway;
import com.kompu.api.entity.usertoken.model.RefreshTokenModel;
import com.kompu.api.entity.usertoken.model.UserSessionModel;
import com.kompu.api.infrastructure.auth.dto.AuthTokenResponse;
import com.kompu.api.infrastructure.auth.dto.AuthTokenResponse.UserAuthResponse;
import com.kompu.api.infrastructure.config.web.security.service.MyUserDetailService;
import com.kompu.api.infrastructure.config.web.security.util.JwtUtils;
import com.kompu.api.infrastructure.shared.SharedUseCase;
import com.kompu.api.usecase.auth.dto.ISignUpRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SignUpUseCase {

    private static final String TENANT_ROLE_ID = "10000000-0000-0000-0000-000000000002";
    private static final String PLATFORM_DOMAIN_SUFFIX = "kompu.id";
    private static final String STATUS_ACTIVE = "active";
    private static final String EMPTY_METADATA = "{}";
    private static final int MEMBER_CODE_MAX_SEQUENCE = 99999;
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 30;
    private static final long JWT_VALIDITY_SECONDS = 604800; // 7 days

    private final SharedUseCase sharedUseCase;
    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final UserRoleGateway userRoleGateway;
    private final MemberGateway memberGateway;
    private final TenantGateway tenantGateway;
    private final TenantDomainGateway tenantDomainGateway;
    private final UserSessionGateway userSessionGateway;
    private final RefreshTokenGateway refreshTokenGateway;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final MyUserDetailService myUserDetailService;

    public SignUpUseCase(
            SharedUseCase sharedUseCase,
            UserGateway userGateway,
            RoleGateway roleGateway,
            UserRoleGateway userRoleGateway,
            MemberGateway memberGateway,
            TenantGateway tenantGateway,
            TenantDomainGateway tenantDomainGateway,
            UserSessionGateway userSessionGateway,
            RefreshTokenGateway refreshTokenGateway,
            BCryptPasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            MyUserDetailService myUserDetailService) {

        this.sharedUseCase = sharedUseCase;
        this.userGateway = userGateway;
        this.roleGateway = roleGateway;
        this.userRoleGateway = userRoleGateway;
        this.memberGateway = memberGateway;
        this.tenantGateway = tenantGateway;
        this.tenantDomainGateway = tenantDomainGateway;
        this.userSessionGateway = userSessionGateway;
        this.refreshTokenGateway = refreshTokenGateway;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.myUserDetailService = myUserDetailService;
    }

    /**
     * Main signup flow with token generation.
     * Creates user, role, member, tenant, domain, session, and returns auth tokens.
     */
    @Transactional
    public AuthTokenResponse execute(ISignUpRequest request) {
        log.info("Starting signup for username: {}", request.username());

        validateSignUpRequest(request);

        UUID tenantId = UUID.randomUUID();

        UserAccountModel user = createUserAccount(request, tenantId);
        assignUserRole(user);
        createMemberRecord(user, request);
        createTenantForUser(request, user.getId());
        setupTenantDomain(request, tenantId);

        AuthTokenResponse response = createUserSessionWithTokens(user, tenantId);

        log.info("Signup completed for user: {} with authentication tokens", user.getUsername());
        return response;
    }

    private void validateSignUpRequest(@SuppressWarnings("unused") ISignUpRequest request) {
        // TODO: Implement validation logic (e.g., check for existing username/email)
    }

    private UserAccountModel createUserAccount(ISignUpRequest request, UUID tenantId) {
        return userGateway.create(
                UserAccountModel.builder()
                        .id(UUID.randomUUID())
                        .tenantId(tenantId)
                        .username(request.username())
                        .email(request.email())
                        .passwordHash(passwordEncoder.encode(request.password()))
                        .fullName(request.fullName())
                        .phone(request.phone())
                        .avatarUrl(request.avatarUrl())
                        .isActive(true)
                        .isEmailVerified(false)
                        .isSystem(false)
                        .build());
    }

    private void assignUserRole(UserAccountModel user) {
        RoleModel role = roleGateway.findById(UUID.fromString(TENANT_ROLE_ID))
                .orElseThrow(() -> new RoleNotFoundException("Default role missing"));

        userRoleGateway.create(
                UserRoleModel.builder()
                        .userId(user.getId())
                        .roleId(role.getId())
                        .assignedAt(LocalDateTime.now())
                        .build());
    }

    private void createMemberRecord(UserAccountModel user, ISignUpRequest request) {
        String memberCode = generateUniqueMemberCode(user.getTenantId());

        memberGateway.create(
                MemberModel.builder()
                        .id(UUID.randomUUID())
                        .tenantId(user.getTenantId())
                        .memberCode(memberCode)
                        .userId(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .address(request.address())
                        .status(STATUS_ACTIVE)
                        .metadata(EMPTY_METADATA)
                        .joinedAt(LocalDate.now())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .createdBy(user.getId().toString())
                        .updatedBy(user.getId().toString())
                        .build());
    }

    private void createTenantForUser(ISignUpRequest request, UUID userId) {
        String tenantName = generateTenantName(request);
        String tenantCode = generateTenantCode(request);
        String metadata = extractTenantMetadata(request);

        // Validate input
        if (tenantName == null || tenantName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant name cannot be empty");
        }
        if (tenantCode == null || tenantCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant code cannot be empty");
        }
        if (userId == null) {
            throw new IllegalArgumentException("Founder user ID cannot be null");
        }

        // Use provided metadata or default to empty JSON object
        String finalMetadata = (metadata != null && !metadata.isEmpty()) ? metadata : "{}";

        // Build the tenant model
        TenantModel newTenant = TenantModel.builder()
                .id(UUID.randomUUID())
                .name(tenantName.trim())
                .code(tenantCode.trim().toLowerCase())
                .status(STATUS_ACTIVE)
                .founderUserId(userId)
                .metadata(finalMetadata)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(userId.toString())
                .updatedBy(userId.toString())
                .build();

        tenantGateway.create(newTenant);
    }

    private void setupTenantDomain(ISignUpRequest request, UUID tenantId) {
        String tenantCode = request.tenantCode();

        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        if (tenantCode == null || tenantCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant code cannot be empty");
        }

        // Generate platform domain
        String host = generatePlatformDomain(tenantCode);

        // Create the primary domain model
        TenantDomainModel domain = TenantDomainModel.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .host(host)
                .primary(true)
                .custom(false)
                .httpsEnabled(true)
                .tlsProvider("cloudflare") // Default to Cloudflare for managed HTTPS
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        tenantDomainGateway.create(domain);
    }

    /**
     * Creates login session and initial token pair.
     * Returns AuthTokenResponse with access and refresh tokens.
     */
    private AuthTokenResponse createUserSessionWithTokens(UserAccountModel user, UUID tenantId) {
        try {
            UserSessionModel session = userSessionGateway.create(
                    UserSessionModel.builder()
                            .id(UUID.randomUUID())
                            .tenantId(tenantId)
                            .userId(user.getId())
                            .createdAt(LocalDateTime.now())
                            .lastActiveAt(LocalDateTime.now())
                            .isActive(true)
                            .build());

            TokenPairResponse tokens = createInitialTokenPair(user, session);

            return buildAuthTokenResponse(user, tokens);

        } catch (Exception e) {
            log.warn("Token creation failed for user {}, creating response without tokens",
                    user.getUsername(), e);
            return buildAuthTokenResponseWithoutTokens(user);
        }
    }

    /**
     * Creates refresh token linked to a session.
     * Access token is generated but NOT persisted (stateless).
     * Returns both access and refresh tokens.
     */
    private TokenPairResponse createInitialTokenPair(UserAccountModel user, UserSessionModel session) {
        var userDetails = myUserDetailService.loadUserByUsername(user.getUsername());

        String accessToken = jwtUtils.generateJwtToken(userDetails);
        log.debug("Access token generated for user: {}", user.getUsername());

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = Base64.getEncoder().encodeToString(rawToken.getBytes());

        refreshTokenGateway.create(
                RefreshTokenModel.builder()
                        .id(UUID.randomUUID())
                        .userId(user.getId())
                        .sessionId(session.getId())
                        .tokenHash(tokenHash)
                        .createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS))
                        .build());

        log.info("Token pair created for user: {}", user.getUsername());

        return new TokenPairResponse(accessToken, rawToken);
    }

    /**
     * Builds AuthTokenResponse from user and tokens.
     */
    private AuthTokenResponse buildAuthTokenResponse(UserAccountModel user, TokenPairResponse tokens) {
        return new AuthTokenResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                JWT_VALIDITY_SECONDS,
                buildUserAuthResponse(user));
    }

    /**
     * Builds AuthTokenResponse without tokens (fallback if token creation failed).
     */
    private AuthTokenResponse buildAuthTokenResponseWithoutTokens(UserAccountModel user) {
        return new AuthTokenResponse(
                null,
                null,
                "Bearer",
                null,
                buildUserAuthResponse(user));
    }

    /**
     * Builds UserAuthResponse from UserAccountModel.
     */
    private UserAuthResponse buildUserAuthResponse(UserAccountModel user) {
        return new UserAuthResponse(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.isEmailVerified(),
                user.getCreatedAt());
    }

    /**
     * Record for token pair response.
     */
    private record TokenPairResponse(String accessToken, String refreshToken) {
    }

    private String generateTenantName(ISignUpRequest request) {
        return request.tenantName().trim();
    }

    private String generateTenantCode(ISignUpRequest request) {
        return request.tenantCode().toLowerCase().trim();
    }

    private String extractTenantMetadata(ISignUpRequest request) {
        return sharedUseCase.toJson(request.tenantMetadata());
    }

    /**
     * Generates tenant-unique member code.
     * Falls back to UUID if sequence exhausted.
     */
    private String generateUniqueMemberCode(UUID tenantId) {
        int year = LocalDate.now().getYear();

        for (int i = 1; i <= MEMBER_CODE_MAX_SEQUENCE; i++) {
            String code = String.format("MEM%d%05d", year, i);
            if (!memberGateway.existsByTenantIdAndMemberCode(tenantId, code)) {
                return code;
            }
        }

        return "MEM" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Generate a platform-provided domain name from tenant code.
     * 
     * Format: "{tenantCode}.kompu.id"
     * 
     * @param tenantCode the tenant code
     * @return the generated domain name
     */
    private String generatePlatformDomain(String tenantCode) {
        return tenantCode.toLowerCase().trim() + "." + PLATFORM_DOMAIN_SUFFIX;
    }

}
