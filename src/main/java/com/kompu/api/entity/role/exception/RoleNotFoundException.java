package com.kompu.api.entity.role.exception;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException() {
        super("Role not found in the system");
    }

    public RoleNotFoundException(String message) {
        super(message);
    }

}
