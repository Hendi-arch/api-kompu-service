package com.kompu.api.infrastructure.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.kompu.api.usecase.auth.dto.IChangePasswordRequest;

/**
 * DTO for password change request
 * 
 * Immutable request object for password change operations.
 * Implements the framework-independent IChangePasswordRequest interface.
 * Uses Jakarta validation annotations for input validation.
 */
public record ChangePasswordRequest(
                @NotBlank(message = "Old password is required") String oldPassword,

                @NotBlank(message = "New password is required") @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters") String newPassword,

                @NotBlank(message = "Password confirmation is required") String confirmPassword)
                implements IChangePasswordRequest {

}
