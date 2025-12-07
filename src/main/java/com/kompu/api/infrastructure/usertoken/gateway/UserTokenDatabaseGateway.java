package com.kompu.api.infrastructure.usertoken.gateway;

import java.util.Optional;

import com.kompu.api.entity.usertoken.gateway.UserTokenGateway;
import com.kompu.api.entity.usertoken.model.UserTokenModel;
import com.kompu.api.infrastructure.config.db.repository.UserTokenRepository;
import com.kompu.api.infrastructure.config.db.schema.UserTokenSchema;

public class UserTokenDatabaseGateway implements UserTokenGateway {

    private final UserTokenRepository repository;

    public UserTokenDatabaseGateway(UserTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserTokenModel create(UserTokenModel userTokenModel) {
        return repository.save(new UserTokenSchema(userTokenModel)).toUserTokenModel();
    }

    @Override
    public void delete(String authToken) {
        repository.deleteByToken(authToken);
    }

    @Override
    public Optional<UserTokenModel> findUserToken(String authToken) {
        return repository.findByToken(authToken).map(UserTokenSchema::toUserTokenModel);
    }

}
