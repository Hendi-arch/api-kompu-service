package com.kompu.api.entity.usertoken.exception;

public class UserTokenNotFoundException extends RuntimeException {

    public UserTokenNotFoundException() {
        super("User token not found in the system");
    }

}
