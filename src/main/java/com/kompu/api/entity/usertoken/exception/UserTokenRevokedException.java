package com.kompu.api.entity.usertoken.exception;

public class UserTokenRevokedException extends RuntimeException {

    public UserTokenRevokedException() {
        super("Unauthorized: Authentication failed or token revoked.");
    }

}
