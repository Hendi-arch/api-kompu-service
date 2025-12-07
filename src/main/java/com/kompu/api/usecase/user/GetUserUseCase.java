package com.kompu.api.usecase.user;

import java.util.UUID;

import com.kompu.api.entity.user.exception.UserNotFoundException;
import com.kompu.api.entity.user.gateway.UserGateway;
import com.kompu.api.entity.user.model.UserAccountModel;

public class GetUserUseCase {

    private final UserGateway userGateway;

    public GetUserUseCase(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    public UserAccountModel findById(UUID id) throws UserNotFoundException {
        return userGateway
                .findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    public UserAccountModel findByUsername(String username) throws UserNotFoundException {
        return userGateway
                .findByUsername(username)
                .orElseThrow(UserNotFoundException::new);
    }

}
