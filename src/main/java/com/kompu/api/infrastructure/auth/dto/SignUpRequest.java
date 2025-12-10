package com.kompu.api.infrastructure.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.kompu.api.usecase.auth.dto.ISignUpRequest;

/**
 * DTO for user registration request
 * 
 * Immutable request object for sign-up operations.
 * Implements the framework-independent ISignUpRequest interface.
 * Uses Jakarta validation annotations for input validation.
 */
public record SignUpRequest(
                @NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username,

                @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email,

                @NotBlank(message = "Password is required") @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters") String password,

                @NotBlank(message = "Password confirmation is required") String confirmPassword,

                @NotBlank(message = "Full name is required") @Size(min = 1, max = 100, message = "Full name must be between 1 and 100 characters") String fullName)
                implements ISignUpRequest {

}
