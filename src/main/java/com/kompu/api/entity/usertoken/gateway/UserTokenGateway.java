package com.kompu.api.entity.usertoken.gateway;

import java.util.Optional;

import com.kompu.api.entity.usertoken.model.UserTokenModel;

public interface UserTokenGateway {

    UserTokenModel create(UserTokenModel userTokenModel);

    void delete(String authToken);

    Optional<UserTokenModel> findUserToken(String authToken);

}
