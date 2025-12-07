package com.kompu.api.entity.userrole.exception;

public class UserRoleNotFoundException extends RuntimeException {

    public UserRoleNotFoundException() {
        super("User Role not found in the system");
    }

}
