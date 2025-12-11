package com.kompu.api.entity.member.exception;

/**
 * MemberNotFoundException is thrown when a requested member cannot be found.
 * 
 * This is a domain-level exception that indicates a business rule violation:
 * the member being accessed does not exist or has been deleted.
 */
public class MemberNotFoundException extends RuntimeException {

    /**
     * Construct exception with a default message
     */
    public MemberNotFoundException() {
        super("Member not found in the system");
    }

    /**
     * Construct exception with a custom message
     * 
     * @param message the error message
     */
    public MemberNotFoundException(String message) {
        super(message);
    }

    /**
     * Construct exception with message and cause
     * 
     * @param message the error message
     * @param cause   the underlying cause
     */
    public MemberNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
