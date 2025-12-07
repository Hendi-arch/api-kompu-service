package com.kompu.api.entity.usertoken.exception;

public class RevokedJtiException extends RuntimeException {

    public RevokedJtiException() {
        super("Token JTI has been revoked");
    }

    public RevokedJtiException(String message) {
        super(message);
    }

}
