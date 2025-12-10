package com.kompu.api.usecase.auth.dto;

/**
 * DTO interface for user authentication request
 * 
 * Framework-independent contract for sign-in operations.
 * This interface is defined in the Use Case layer and should not have any
 * Spring or validation framework dependencies.
 * 
 * Implementations of this interface should be in the Infrastructure layer
 * with proper validation annotations.
 */
public interface ISignInRequest {

    String username();

    String password();

}
