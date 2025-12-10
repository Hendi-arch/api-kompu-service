package com.kompu.api.infrastructure.config.web.mvc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.kompu.api.entity.appconfig.gateway.AppConfigGateway;
import com.kompu.api.entity.user.gateway.UserGateway;
import com.kompu.api.entity.usertoken.gateway.RefreshTokenGateway;
import com.kompu.api.entity.usertoken.gateway.UserSessionGateway;
import com.kompu.api.entity.usertoken.gateway.UserTokenGateway;
import com.kompu.api.infrastructure.config.db.repository.AppConfigRepository;
import com.kompu.api.infrastructure.config.db.repository.RefreshTokenRepository;
import com.kompu.api.infrastructure.config.db.repository.UserRepository;
import com.kompu.api.infrastructure.config.db.repository.UserSessionRepository;
import com.kompu.api.infrastructure.config.db.repository.UserTokenRepository;
import com.kompu.api.infrastructure.appconfig.gateway.AppConfigDatabaseGateway;
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

}
