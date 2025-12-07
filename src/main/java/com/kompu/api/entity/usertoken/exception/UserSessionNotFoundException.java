package com.kompu.api.entity.usertoken.exception;

public class UserSessionNotFoundException extends RuntimeException {

    public UserSessionNotFoundException() {
        super("User session not found in the system");
    }

    public UserSessionNotFoundException(String message) {
        super(message);
    }

}
