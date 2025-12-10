package com.kompu.api.infrastructure.auth.dto;

import jakarta.validation.constraints.NotBlank;

import com.kompu.api.usecase.auth.dto.ISignInRequest;

/**
 * DTO for user authentication request
 * 
 * Immutable request object for sign-in operations.
 * Implements the framework-independent ISignInRequest interface.
 * Uses Jakarta validation annotations for input validation.
 */
public record SignInRequest(
                @NotBlank(message = "Username is required") String username,

                @NotBlank(message = "Password is required") String password) implements ISignInRequest {

}
