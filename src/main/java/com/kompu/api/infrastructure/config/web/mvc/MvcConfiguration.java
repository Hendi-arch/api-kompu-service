package com.kompu.api.infrastructure.config.web.mvc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kompu.api.entity.user.gateway.UserGateway;
import com.kompu.api.entity.usertoken.gateway.UserTokenGateway;
import com.kompu.api.infrastructure.config.db.repository.UserRepository;
import com.kompu.api.infrastructure.config.db.repository.UserTokenRepository;
import com.kompu.api.infrastructure.user.gateway.UserDatabaseGateway;
import com.kompu.api.infrastructure.usertoken.gateway.UserTokenDatabaseGateway;
import com.kompu.api.usecase.user.GetUserUseCase;
import com.kompu.api.usecase.usertoken.GetUserTokenUseCase;

@Configuration
public class MvcConfiguration {

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

}
