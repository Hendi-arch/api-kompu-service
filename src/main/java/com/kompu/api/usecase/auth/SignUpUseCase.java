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
import com.kompu.api.entity.subscription.gateway.SubscriptionPlanGateway;
import com.kompu.api.entity.subscription.gateway.TenantRegistrationGateway;
import com.kompu.api.entity.subscription.gateway.TenantSubscriptionGateway;
import com.kompu.api.entity.subscription.model.SubscriptionPlanModel;
import com.kompu.api.entity.subscription.model.TenantRegistrationModel;
import com.kompu.api.entity.subscription.model.TenantSubscriptionModel;
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

import com.kompu.api.entity.shared.gateway.FileStorageGateway;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SignUpUseCase {

    private static final String SYSTEM_ROLE_ID = "10000000-0000-0000-0000-000000000002"; // Global System Role
    private static final String PLATFORM_DOMAIN_SUFFIX = "kompu.id";
    private static final String STATUS_ACTIVE = "active";
    private static final String DEFAULT_PLAN = "BASIC_MONTHLY";
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
    private final TenantRegistrationGateway tenantRegistrationGateway;
    private final TenantSubscriptionGateway tenantSubscriptionGateway;
    private final SubscriptionPlanGateway subscriptionPlanGateway;
    private final UserSessionGateway userSessionGateway;
    private final RefreshTokenGateway refreshTokenGateway;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final MyUserDetailService myUserDetailService;
    private final FileStorageGateway fileStorageGateway;

    public SignUpUseCase(
            SharedUseCase sharedUseCase,
            UserGateway userGateway,
            RoleGateway roleGateway,
            UserRoleGateway userRoleGateway,
            MemberGateway memberGateway,
            TenantGateway tenantGateway,
            TenantDomainGateway tenantDomainGateway,
            TenantRegistrationGateway tenantRegistrationGateway,
            TenantSubscriptionGateway tenantSubscriptionGateway,
            SubscriptionPlanGateway subscriptionPlanGateway,
            UserSessionGateway userSessionGateway,
            RefreshTokenGateway refreshTokenGateway,
            BCryptPasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            MyUserDetailService myUserDetailService,
            FileStorageGateway fileStorageGateway) {

        this.sharedUseCase = sharedUseCase;
        this.userGateway = userGateway;
        this.roleGateway = roleGateway;
        this.userRoleGateway = userRoleGateway;
        this.memberGateway = memberGateway;
        this.tenantGateway = tenantGateway;
        this.tenantDomainGateway = tenantDomainGateway;
        this.tenantRegistrationGateway = tenantRegistrationGateway;
        this.tenantSubscriptionGateway = tenantSubscriptionGateway;
        this.subscriptionPlanGateway = subscriptionPlanGateway;
        this.userSessionGateway = userSessionGateway;
        this.refreshTokenGateway = refreshTokenGateway;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.myUserDetailService = myUserDetailService;
        this.fileStorageGateway = fileStorageGateway;
    }

    /**
     * Main signup flow with token generation.
     * Orchestrates creation of Tenant, Domain, Roles, Subscription, User, Member,
     * Registration Log, and Session.
     */
    @Transactional
    public AuthTokenResponse execute(ISignUpRequest request) {
        log.info("Starting signup for email: {}", request.email());

        validateSignUpRequest(request);

        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID(); // Pre-generate to link entities

        // 1. Create Tenant
        createTenantForUser(request, tenantId, userId);

        // 2. Create Tenant Domain
        setupTenantDomain(request, tenantId);

        // 4. Setup Subscription
        setupTenantSubscription(request, tenantId, userId);

        // 5. Create User Account
        UserAccountModel user = createUserAccount(request, userId, tenantId);

        // 6. Assign Admin Role to User
        assignUserRole(user);

        // 7. Create Member Record
        createMemberRecord(user, request);

        // 8. Record Registration Audit
        recordTenantRegistration(request, user, tenantId);

        // 9. Create Session & Tokens
        AuthTokenResponse response = createUserSessionWithTokens(user, tenantId);

        log.info("Signup completed for user: {} with authentication tokens", user.getEmail());
        return response;
    }

    private void validateSignUpRequest(ISignUpRequest request) {
        // TODO: Validate request
    }

    private void createTenantForUser(ISignUpRequest request, UUID tenantId, UUID userId) {
        String tenantName = request.tenantName() != null ? request.tenantName().trim()
                : request.email().split("@")[0] + "'s Organization";
        String tenantCode = request.tenantCode() != null ? request.tenantCode().trim().toLowerCase()
                : request.email().split("@")[0].toLowerCase();

        Map<String, Object> processedMetadata = processTenantMetadata(request.tenantMetadata(), tenantId);
        String metadata = sharedUseCase.toJson(processedMetadata);
        String finalMetadata = (metadata != null && !metadata.isEmpty()) ? metadata : "{}";

        TenantModel newTenant = TenantModel.builder()
                .id(tenantId)
                .name(tenantName)
                .code(tenantCode)
                .status(STATUS_ACTIVE)
                .founderUserId(userId)
                .metadata(finalMetadata)
                .themeId(request.themeId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(userId.toString())
                .updatedBy(userId.toString())
                .build();

        tenantGateway.create(newTenant);
    }

    private void setupTenantDomain(ISignUpRequest request, UUID tenantId) {
        String tenantCode = request.tenantCode() != null ? request.tenantCode()
                : request.email().split("@")[0].toLowerCase();
        String host = tenantCode.toLowerCase().trim() + "." + PLATFORM_DOMAIN_SUFFIX;

        TenantDomainModel domain = TenantDomainModel.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .host(host)
                .primary(true)
                .custom(false)
                .httpsEnabled(true)
                .tlsProvider("cloudflare")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        tenantDomainGateway.create(domain);
    }

    private void setupTenantSubscription(ISignUpRequest request, UUID tenantId, UUID userId) {
        String planName = request.planName() != null ? request.planName() : DEFAULT_PLAN;

        SubscriptionPlanModel plan = subscriptionPlanGateway.findByName(planName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid subscription plan: " + planName));

        TenantSubscriptionModel subscription = TenantSubscriptionModel.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .planId(plan.getId())
                .subscriptionStartDate(LocalDate.now())
                .status(STATUS_ACTIVE)
                .autoRenew(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(userId.toString())
                .updatedBy(userId.toString())
                .build();

        tenantSubscriptionGateway.save(subscription);
    }

    private UserAccountModel createUserAccount(ISignUpRequest request, UUID userId, UUID tenantId) {
        return userGateway.create(
                UserAccountModel.builder()
                        .id(userId)
                        .tenantId(tenantId)
                        .email(request.email())
                        .passwordHash(passwordEncoder.encode(request.password()))
                        .fullName(request.fullName())
                        .phone(request.phone())
                        .avatarUrl(request.avatarUrl())
                        .isActive(true)
                        .isEmailVerified(false)
                        .isSystem(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .createdBy(userId.toString())
                        .updatedBy(userId.toString())
                        .build());
    }

    private void assignUserRole(UserAccountModel user) {
        RoleModel role = roleGateway.findById(UUID.fromString(SYSTEM_ROLE_ID))
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
                        .metadata("{}")
                        .joinedAt(LocalDate.now())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .createdBy(user.getId().toString())
                        .updatedBy(user.getId().toString())
                        .build());
    }

    private void recordTenantRegistration(ISignUpRequest request, UserAccountModel user, UUID tenantId) {
        tenantRegistrationGateway.save(
                TenantRegistrationModel.builder()
                        .id(UUID.randomUUID())
                        .tenantId(tenantId)
                        .registrationType(request.registrationType())
                        .adminUserId(user.getId())
                        .emailUsed(request.email())
                        .ipAddress(request.ipAddress())
                        .userAgent(request.userAgent())
                        .termsAcceptedAt(request.termsAcceptedAt())
                        .privacyAcceptedAt(request.privacyAcceptedAt())
                        .registrationSource(request.registrationSource())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
    }

    private AuthTokenResponse createUserSessionWithTokens(UserAccountModel user, UUID tenantId) {
        try {
            UserSessionModel session = userSessionGateway.create(
                    UserSessionModel.builder()
                            .id(UUID.randomUUID())
                            .tenantId(tenantId)
                            .userId(user.getId())
                            .ipAddress(null) // Ideally passed from controller
                            .userAgent(null) // Ideally passed from controller
                            .createdAt(LocalDateTime.now())
                            .lastActiveAt(LocalDateTime.now())
                            .isActive(true)
                            .build());

            TokenPairResponse tokens = createInitialTokenPair(user, session);

            return buildAuthTokenResponse(user, tokens);

        } catch (Exception e) {
            log.warn("Token creation failed for user {}, creating response without tokens",
                    user.getEmail(), e);
            return buildAuthTokenResponseWithoutTokens(user);
        }
    }

    private TokenPairResponse createInitialTokenPair(UserAccountModel user, UserSessionModel session) {
        var userDetails = myUserDetailService.loadUserByUsername(user.getId().toString());

        String accessToken = jwtUtils.generateJwtToken(userDetails);

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

        return new TokenPairResponse(accessToken, rawToken);
    }

    private AuthTokenResponse buildAuthTokenResponse(UserAccountModel user, TokenPairResponse tokens) {
        return new AuthTokenResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                JWT_VALIDITY_SECONDS,
                buildUserAuthResponse(user));
    }

    private AuthTokenResponse buildAuthTokenResponseWithoutTokens(UserAccountModel user) {
        return new AuthTokenResponse(
                null,
                null,
                "Bearer",
                null,
                buildUserAuthResponse(user));
    }

    private UserAuthResponse buildUserAuthResponse(UserAccountModel user) {
        return new UserAuthResponse(
                user.getId().toString(),
                user.getEmail(), // Use email as "username" for response
                user.getFullName(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.isEmailVerified(),
                user.getCreatedAt());
    }

    private record TokenPairResponse(String accessToken, String refreshToken) {
    }

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

    private Map<String, Object> processTenantMetadata(Map<String, Object> metadata, UUID tenantId) {
        if (metadata == null || metadata.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> updatedMetadata = new HashMap<>(metadata);
        String key = "profilePhotoBase64";

        if (updatedMetadata.containsKey(key)) {
            Object value = updatedMetadata.get(key);
            if (value instanceof String base64Content && !((String) value).isEmpty()) {
                // Save file and get path
                String filePath = fileStorageGateway.saveBase64File(
                        "profile_photo", base64Content, tenantId.toString());

                if (filePath != null) {
                    updatedMetadata.put(key, filePath);
                }
            }
        }

        return updatedMetadata;
    }
}
