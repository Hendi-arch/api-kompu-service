package com.kompu.api.entity.usertoken.exception;

public class RefreshTokenNotFoundException extends RuntimeException {

    public RefreshTokenNotFoundException() {
        super("Refresh token not found in the system");
    }

    public RefreshTokenNotFoundException(String message) {
        super(message);
    }

}
