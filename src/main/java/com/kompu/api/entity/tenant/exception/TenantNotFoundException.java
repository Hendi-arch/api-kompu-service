package com.kompu.api.entity.tenant.exception;

/**
 * TenantNotFoundException is thrown when a requested tenant cannot be found.
 * 
 * This is a domain-level exception that indicates a business rule violation:
 * the tenant being accessed does not exist or has been deleted.
 */
public class TenantNotFoundException extends RuntimeException {

    /**
     * Construct exception with a default message
     */
    public TenantNotFoundException() {
        super("Tenant not found in the system");
    }

    /**
     * Construct exception with a custom message
     * 
     * @param message the error message
     */
    public TenantNotFoundException(String message) {
        super(message);
    }

    /**
     * Construct exception with message and cause
     * 
     * @param message the error message
     * @param cause   the underlying cause
     */
    public TenantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
