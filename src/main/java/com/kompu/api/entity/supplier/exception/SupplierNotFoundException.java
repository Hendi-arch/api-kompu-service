package com.kompu.api.entity.supplier.exception;

/**
 * SupplierNotFoundException - Thrown when supplier is not found
 */
public class SupplierNotFoundException extends RuntimeException {

    public SupplierNotFoundException(String message) {
        super(message);
    }

    public SupplierNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SupplierNotFoundException() {
        super("Supplier not found in the system");
    }
}
