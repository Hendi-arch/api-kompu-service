package com.kompu.api.infrastructure.config.web.mvc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.kompu.api.infrastructure.shared.SharedUseCase;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kompu.api.entity.appconfig.gateway.AppConfigGateway;
import com.kompu.api.entity.shared.gateway.FileStorageGateway;
import com.kompu.api.entity.usertoken.gateway.UserSessionGateway;
import com.kompu.api.entity.usertoken.gateway.UserTokenGateway;
import com.kompu.api.entity.user.gateway.UserGateway;
import com.kompu.api.entity.usertoken.gateway.RefreshTokenGateway;
import com.kompu.api.infrastructure.appconfig.gateway.AppConfigDatabaseGateway;
import com.kompu.api.infrastructure.config.db.repository.AppConfigRepository;
import com.kompu.api.infrastructure.config.db.repository.RefreshTokenRepository;
import com.kompu.api.infrastructure.config.db.repository.UserRepository;
import com.kompu.api.infrastructure.config.db.repository.UserSessionRepository;
import com.kompu.api.infrastructure.config.db.repository.UserTokenRepository;
import com.kompu.api.infrastructure.config.web.security.util.JwtUtils;
import com.kompu.api.infrastructure.user.gateway.UserDatabaseGateway;
import com.kompu.api.infrastructure.usertoken.gateway.RefreshTokenDatabaseGateway;
import com.kompu.api.infrastructure.usertoken.gateway.UserSessionDatabaseGateway;
import com.kompu.api.infrastructure.usertoken.gateway.UserTokenDatabaseGateway;
import com.kompu.api.usecase.appconfig.RsaKeyPairUseCase;
import com.kompu.api.usecase.user.ChangePasswordUseCase;
import com.kompu.api.usecase.user.CreateUserUseCase;
import com.kompu.api.usecase.user.GetUserUseCase;
import com.kompu.api.usecase.user.ValidateUserCredentialsUseCase;
import com.kompu.api.usecase.usertoken.CreateUserSessionUseCase;
import com.kompu.api.usecase.usertoken.GenerateAccessTokenUseCase;
import com.kompu.api.usecase.usertoken.GenerateRefreshTokenUseCase;
import com.kompu.api.usecase.usertoken.GetUserTokenUseCase;
import com.kompu.api.usecase.usertoken.ValidateRefreshTokenUseCase;

@Configuration
public class MvcConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        objectMapper.registerModule(javaTimeModule);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    @Bean
    public RsaKeyPairUseCase rsaKeyPairUseCase(AppConfigRepository appConfigRepository) {
        AppConfigGateway appConfigGateway = new AppConfigDatabaseGateway(appConfigRepository);
        return new RsaKeyPairUseCase(appConfigGateway);
    }

    @Bean
    public GetUserUseCase getUserUseCase(UserRepository userRepository) {
        UserGateway userGateway = new UserDatabaseGateway(userRepository);
        return new GetUserUseCase(userGateway);
    }

    @Bean
    public GetUserTokenUseCase getUserTokenUseCase(UserTokenRepository userTokenRepository) {
        UserTokenGateway userTokenGateway = new UserTokenDatabaseGateway(userTokenRepository);
        return new GetUserTokenUseCase(userTokenGateway);
    }

    // ==================== Gateway Beans ====================

    @Bean
    public RefreshTokenGateway refreshTokenGateway(RefreshTokenRepository refreshTokenRepository) {
        return new RefreshTokenDatabaseGateway(refreshTokenRepository);
    }

    @Bean
    public UserSessionGateway userSessionGateway(UserSessionRepository userSessionRepository) {
        return new UserSessionDatabaseGateway(userSessionRepository);
    }

    // ==================== Authentication Use Cases ====================

    @Bean
    public CreateUserUseCase createUserUseCase(UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder) {
        UserGateway userGateway = new UserDatabaseGateway(userRepository);
        return new CreateUserUseCase(userGateway, passwordEncoder);
    }

    @Bean
    public ValidateUserCredentialsUseCase validateUserCredentialsUseCase(UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder) {
        UserGateway userGateway = new UserDatabaseGateway(userRepository);
        return new ValidateUserCredentialsUseCase(userGateway, passwordEncoder);
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder) {
        UserGateway userGateway = new UserDatabaseGateway(userRepository);
        return new ChangePasswordUseCase(userGateway, passwordEncoder);
    }

    @Bean
    public GenerateAccessTokenUseCase generateAccessTokenUseCase(JwtUtils jwtUtils) {
        return new GenerateAccessTokenUseCase(jwtUtils);
    }

    @Bean
    public GenerateRefreshTokenUseCase generateRefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository) {
        RefreshTokenGateway refreshTokenGateway = new RefreshTokenDatabaseGateway(refreshTokenRepository);
        return new GenerateRefreshTokenUseCase(refreshTokenGateway);
    }

    @Bean
    public ValidateRefreshTokenUseCase validateRefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository) {
        RefreshTokenGateway refreshTokenGateway = new RefreshTokenDatabaseGateway(refreshTokenRepository);
        return new ValidateRefreshTokenUseCase(refreshTokenGateway);
    }

    @Bean
    public CreateUserSessionUseCase createUserSessionUseCase(UserSessionRepository userSessionRepository) {
        UserSessionGateway userSessionGateway = new UserSessionDatabaseGateway(userSessionRepository);
        return new CreateUserSessionUseCase(userSessionGateway);
    }

    // ==================== Shared Beans ====================

    @Bean
    public SharedUseCase sharedUseCase(ObjectMapper objectMapper) {
        return new com.kompu.api.infrastructure.shared.SharedUseCase(objectMapper);
    }

    // ==================== Complete Auth Use Cases ====================

    @Bean
    public com.kompu.api.usecase.auth.SignUpUseCase signUpUseCase(
            com.kompu.api.infrastructure.shared.SharedUseCase sharedUseCase,
            UserRepository userRepository,
            com.kompu.api.infrastructure.config.db.repository.RoleRepository roleRepository,
            com.kompu.api.infrastructure.config.db.repository.UserRoleRepository userRoleRepository,
            com.kompu.api.infrastructure.config.db.repository.MemberRepository memberRepository,
            com.kompu.api.infrastructure.config.db.repository.TenantRepository tenantRepository,
            com.kompu.api.infrastructure.config.db.repository.TenantDomainRepository tenantDomainRepository,
            com.kompu.api.infrastructure.config.db.repository.TenantRegistrationRepository tenantRegistrationRepository,
            com.kompu.api.infrastructure.config.db.repository.TenantSubscriptionRepository tenantSubscriptionRepository,
            com.kompu.api.infrastructure.config.db.repository.SubscriptionPlanRepository subscriptionPlanRepository,
            UserSessionRepository userSessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            com.kompu.api.infrastructure.config.web.security.service.MyUserDetailService myUserDetailService,
            FileStorageGateway fileStorageGateway) {

        UserGateway userGateway = new UserDatabaseGateway(userRepository);
        com.kompu.api.entity.role.gateway.RoleGateway roleGateway = new com.kompu.api.infrastructure.role.gateway.RoleDatabaseGateway(
                roleRepository);
        com.kompu.api.entity.user.gateway.UserRoleGateway userRoleGateway = new com.kompu.api.infrastructure.user.gateway.UserRoleDatabaseGateway(
                userRoleRepository);
        com.kompu.api.entity.member.gateway.MemberGateway memberGateway = new com.kompu.api.infrastructure.member.gateway.MemberDatabaseGateway(
                memberRepository);
        com.kompu.api.entity.tenant.gateway.TenantGateway tenantGateway = new com.kompu.api.infrastructure.tenant.gateway.TenantDatabaseGateway(
                tenantRepository);
        com.kompu.api.entity.tenantdomain.gateway.TenantDomainGateway tenantDomainGateway = new com.kompu.api.infrastructure.tenantdomain.gateway.TenantDomainDatabaseGateway(
                tenantDomainRepository);
        com.kompu.api.entity.subscription.gateway.TenantRegistrationGateway tenantRegistrationGateway = new com.kompu.api.infrastructure.subscription.gateway.TenantRegistrationDatabaseGateway(
                tenantRegistrationRepository);
        com.kompu.api.entity.subscription.gateway.TenantSubscriptionGateway tenantSubscriptionGateway = new com.kompu.api.infrastructure.subscription.gateway.TenantSubscriptionDatabaseGateway(
                tenantSubscriptionRepository);
        com.kompu.api.entity.subscription.gateway.SubscriptionPlanGateway subscriptionPlanGateway = new com.kompu.api.infrastructure.subscription.gateway.SubscriptionPlanDatabaseGateway(
                subscriptionPlanRepository);
        UserSessionGateway userSessionGateway = new UserSessionDatabaseGateway(userSessionRepository);
        RefreshTokenGateway refreshTokenGateway = new RefreshTokenDatabaseGateway(refreshTokenRepository);

        return new com.kompu.api.usecase.auth.SignUpUseCase(
                sharedUseCase,
                userGateway,
                roleGateway,
                userRoleGateway,
                memberGateway,
                tenantGateway,
                tenantDomainGateway,
                tenantRegistrationGateway,
                tenantSubscriptionGateway,
                subscriptionPlanGateway,
                userSessionGateway,
                refreshTokenGateway,
                passwordEncoder,
                jwtUtils,
                myUserDetailService,
                fileStorageGateway);
    }

    @Bean
    public com.kompu.api.usecase.auth.SignInUseCase signInUseCase(
            ValidateUserCredentialsUseCase validateUserCredentialsUseCase,
            CreateUserSessionUseCase createUserSessionUseCase,
            GenerateAccessTokenUseCase generateAccessTokenUseCase,
            GenerateRefreshTokenUseCase generateRefreshTokenUseCase,
            com.kompu.api.infrastructure.config.db.repository.LoginLogRepository loginLogRepository) {

        com.kompu.api.entity.system.gateway.LoginLogGateway loginLogGateway = new com.kompu.api.infrastructure.system.gateway.LoginLogDatabaseGateway(
                loginLogRepository);

        return new com.kompu.api.usecase.auth.SignInUseCase(
                validateUserCredentialsUseCase,
                createUserSessionUseCase,
                generateAccessTokenUseCase,
                generateRefreshTokenUseCase,
                loginLogGateway);
    }

    @Bean
    public com.kompu.api.usecase.auth.RefreshTokenUseCase refreshTokenUseCase(
            ValidateRefreshTokenUseCase validateRefreshTokenUseCase,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            GenerateAccessTokenUseCase generateAccessTokenUseCase,
            GenerateRefreshTokenUseCase generateRefreshTokenUseCase) {

        RefreshTokenGateway refreshTokenGateway = new RefreshTokenDatabaseGateway(refreshTokenRepository);
        UserGateway userGateway = new UserDatabaseGateway(userRepository);

        return new com.kompu.api.usecase.auth.RefreshTokenUseCase(
                validateRefreshTokenUseCase,
                refreshTokenGateway,
                userGateway,
                generateAccessTokenUseCase,
                generateRefreshTokenUseCase);
    }

    @Bean
    public com.kompu.api.usecase.auth.ForgotPasswordUseCase forgotPasswordUseCase(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            JavaMailSender emailSender) {

        UserGateway userGateway = new UserDatabaseGateway(userRepository);
        return new com.kompu.api.usecase.auth.ForgotPasswordUseCase(userGateway, passwordEncoder, emailSender);
    }

}
