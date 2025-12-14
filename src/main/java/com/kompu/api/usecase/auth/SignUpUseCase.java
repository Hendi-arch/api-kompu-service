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
import com.kompu.api.entity.user.gateway.UserGateway;
import com.kompu.api.entity.user.gateway.UserRoleGateway;
import com.kompu.api.entity.user.model.UserAccountModel;
import com.kompu.api.entity.user.model.UserRoleModel;
import com.kompu.api.entity.usertoken.gateway.RefreshTokenGateway;
import com.kompu.api.entity.usertoken.gateway.UserSessionGateway;
import com.kompu.api.entity.usertoken.model.RefreshTokenModel;
import com.kompu.api.entity.usertoken.model.UserSessionModel;
import com.kompu.api.infrastructure.config.web.security.service.MyUserDetailService;
import com.kompu.api.infrastructure.config.web.security.util.JwtUtils;
import com.kompu.api.usecase.auth.dto.ISignUpRequest;
import com.kompu.api.usecase.tenant.CreateTenantUseCase;
import com.kompu.api.usecase.tenantdomain.SetupTenantDomainUseCase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SignUpUseCase {

    private static final String TENANT_ROLE_ID = "10000000-0000-0000-0000-000000000002";
    private static final String MEMBER_STATUS_ACTIVE = "active";
    private static final String EMPTY_METADATA = "{}";
    private static final int MEMBER_CODE_MAX_SEQUENCE = 99999;
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 30;

    private final CreateTenantUseCase createTenantUseCase;
    private final SetupTenantDomainUseCase setupTenantDomainUseCase;
    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final UserRoleGateway userRoleGateway;
    private final MemberGateway memberGateway;
    private final UserSessionGateway userSessionGateway;
    private final RefreshTokenGateway refreshTokenGateway;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final MyUserDetailService myUserDetailService;

    public SignUpUseCase(
            CreateTenantUseCase createTenantUseCase,
            SetupTenantDomainUseCase setupTenantDomainUseCase,
            UserGateway userGateway,
            RoleGateway roleGateway,
            UserRoleGateway userRoleGateway,
            MemberGateway memberGateway,
            UserSessionGateway userSessionGateway,
            RefreshTokenGateway refreshTokenGateway,
            BCryptPasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            MyUserDetailService myUserDetailService) {

        this.createTenantUseCase = createTenantUseCase;
        this.setupTenantDomainUseCase = setupTenantDomainUseCase;
        this.userGateway = userGateway;
        this.roleGateway = roleGateway;
        this.userRoleGateway = userRoleGateway;
        this.memberGateway = memberGateway;
        this.userSessionGateway = userSessionGateway;
        this.refreshTokenGateway = refreshTokenGateway;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.myUserDetailService = myUserDetailService;
    }

    @Transactional
    public UserAccountModel execute(ISignUpRequest request) {
        return execute(request, null, null);
    }

    /**
     * Main signup flow.
     * Creates user, role, member, tenant, domain, and optional session.
     */
    @Transactional
    public UserAccountModel execute(ISignUpRequest request, String ipAddress, String userAgent) {
        log.info("Starting signup for username: {}", request.username());

        validateSignUpRequest(request);

        UUID tenantId = UUID.randomUUID();

        UserAccountModel user = createUserAccount(request, tenantId);
        assignUserRole(user);
        createMemberRecord(user, request);
        createTenantForUser(request, user.getId());
        setupTenantDomain(request, tenantId);

        if (ipAddress != null || userAgent != null) {
            createUserSession(user, tenantId, ipAddress, userAgent);
        }

        log.info("Signup completed for user: {}", user.getUsername());
        return user;
    }

    private void validateSignUpRequest(ISignUpRequest request) {
        if (request == null)
            throw new IllegalArgumentException("Request is null");
        if (isBlank(request.username()))
            throw new IllegalArgumentException("Username required");
        if (isBlank(request.email()))
            throw new IllegalArgumentException("Email required");
        if (isBlank(request.password()))
            throw new IllegalArgumentException("Password required");
        if (isBlank(request.fullName()))
            throw new IllegalArgumentException("Full name required");
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
                        .status(MEMBER_STATUS_ACTIVE)
                        .metadata(EMPTY_METADATA)
                        .joinedAt(LocalDate.now())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .createdBy(user.getId().toString())
                        .updatedBy(user.getId().toString())
                        .build());
    }

    private void createTenantForUser(ISignUpRequest request, UUID userId) {
        createTenantUseCase.createTenantWithMetadata(
                generateTenantName(request),
                generateTenantCode(request),
                userId,
                extractTenantMetadata(request));
    }

    private void setupTenantDomain(ISignUpRequest request, UUID tenantId) {
        String tenantCode = isBlank(request.tenantCode())
                ? request.username().toLowerCase()
                : request.tenantCode().toLowerCase();

        setupTenantDomainUseCase.setupInitialDomain(tenantId, tenantCode);
    }

    private String generateTenantName(ISignUpRequest request) {
        return isBlank(request.tenantName())
                ? request.fullName() + "'s Organization"
                : request.tenantName().trim();
    }

    private String generateTenantCode(ISignUpRequest request) {
        return isBlank(request.tenantCode())
                ? request.username().toLowerCase()
                : request.tenantCode().toLowerCase().trim();
    }

    private String extractTenantMetadata(ISignUpRequest request) {
        return isBlank(request.tenantMetadata())
                ? EMPTY_METADATA
                : request.tenantMetadata().trim();
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
     * Creates login session and initial refresh token.
     * Failure here does NOT block signup.
     */
    private void createUserSession(
            UserAccountModel user,
            UUID tenantId,
            String ipAddress,
            String userAgent) {

        try {
            UserSessionModel session = userSessionGateway.create(
                    UserSessionModel.builder()
                            .id(UUID.randomUUID())
                            .tenantId(tenantId)
                            .userId(user.getId())
                            .ipAddress(ipAddress)
                            .userAgent(userAgent)
                            .createdAt(LocalDateTime.now())
                            .lastActiveAt(LocalDateTime.now())
                            .isActive(true)
                            .build());

            createInitialRefreshToken(user, session);

        } catch (Exception e) {
            log.warn("Session creation failed for user {}, fallback to manual login",
                    user.getUsername(), e);
        }
    }

    /**
     * Creates refresh token linked to a session.
     * Access token is generated but NOT persisted.
     */
    private void createInitialRefreshToken(UserAccountModel user, UserSessionModel session) {
        try {
            var userDetails = myUserDetailService.loadUserByUsername(user.getUsername());

            jwtUtils.generateJwtToken(userDetails); // access token (returned to client)

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

        } catch (Exception e) {
            log.warn("Token creation failed for user {}, user must login manually",
                    user.getUsername(), e);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

}
