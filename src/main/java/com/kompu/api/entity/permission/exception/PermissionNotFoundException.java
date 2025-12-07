package com.kompu.api.entity.permission.exception;

public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException() {
        super("Permission not found in the system");
    }

    public PermissionNotFoundException(String message) {
        super(message);
    }

}
