package com.kompu.api.usecase.auth.dto;

/**
 * DTO interface for password change request
 * 
 * Framework-independent contract for password change operations.
 * This interface is defined in the Use Case layer and should not have any
 * Spring or validation framework dependencies.
 * 
 * Implementations of this interface should be in the Infrastructure layer
 * with proper validation annotations.
 */
public interface IChangePasswordRequest {

    String oldPassword();

    String newPassword();

    String confirmPassword();

}
