package com.kompu.api.usecase.auth.dto;

import java.time.LocalDateTime;

/**
 * DTO interface for authentication token response
 * 
 * Framework-independent contract for authentication responses.
 * This interface is defined in the Use Case layer and should not have any
 * Spring or validation framework dependencies.
 * 
 * Implementations of this interface should be in the Infrastructure layer
 * with proper JSON serialization annotations.
 */
public interface IAuthTokenResponse {

    String accessToken();

    String refreshToken();

    String tokenType();

    Long expiresIn();

    IUserAuthResponse user();

    /**
     * Nested DTO interface for user authentication information
     */
    interface IUserAuthResponse {

        String id();

        String username();

        String email();

        String fullName();

        String phone();

        String avatarUrl();

        boolean isEmailVerified();

        LocalDateTime createdAt();

    }

}
