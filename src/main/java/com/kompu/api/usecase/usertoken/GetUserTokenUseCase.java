package com.kompu.api.usecase.usertoken;

import com.kompu.api.entity.usertoken.exception.UserTokenNotFoundException;
import com.kompu.api.entity.usertoken.gateway.UserTokenGateway;
import com.kompu.api.entity.usertoken.model.UserTokenModel;

public class GetUserTokenUseCase {

    private final UserTokenGateway userTokenGateway;

    public GetUserTokenUseCase(UserTokenGateway userTokenGateway) {
        this.userTokenGateway = userTokenGateway;
    }

    public UserTokenModel execute(String authToken) throws UserTokenNotFoundException {
        return userTokenGateway.findUserToken(authToken).orElseThrow(UserTokenNotFoundException::new);
    }

}
