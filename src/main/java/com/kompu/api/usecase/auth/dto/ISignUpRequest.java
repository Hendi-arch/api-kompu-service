package com.kompu.api.usecase.auth.dto;

/**
 * DTO interface for user registration request
 * 
 * Framework-independent contract for sign-up operations.
 * This interface is defined in the Use Case layer and should not have any
 * Spring or validation framework dependencies.
 * 
 * Implementations of this interface should be in the Infrastructure layer
 * with proper validation annotations.
 */
public interface ISignUpRequest {

    String username();

    String email();

    String password();

    String confirmPassword();

    String fullName();

}
