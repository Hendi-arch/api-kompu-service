package com.kompu.api.infrastructure.auth.dto;

import jakarta.validation.constraints.NotBlank;

import com.kompu.api.usecase.auth.dto.IRefreshTokenRequest;

/**
 * DTO for token refresh request
 * 
 * Immutable request object for token refresh operations.
 * Implements the framework-independent IRefreshTokenRequest interface.
 * Uses Jakarta validation annotations for input validation.
 */
public record RefreshTokenRequest(
                @NotBlank(message = "Refresh token is required") String refreshToken) implements IRefreshTokenRequest {

}
