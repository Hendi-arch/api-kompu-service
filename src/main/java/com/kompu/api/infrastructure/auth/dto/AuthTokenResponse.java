package com.kompu.api.infrastructure.auth.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kompu.api.usecase.auth.dto.IAuthTokenResponse;

/**
 * DTO for authentication token response
 * 
 * Immutable response object for authentication operations.
 * Implements the framework-independent IAuthTokenResponse interface.
 * Serialized with snake_case JSON property names for API consistency.
 * 
 * @param accessToken  JWT access token
 * @param refreshToken Refresh token for token renewal
 * @param tokenType    Bearer token type
 * @param expiresIn    Token expiration time in seconds (7 days)
 * @param user         Authenticated user information
 */
public record AuthTokenResponse(
                @JsonProperty("access_token") String accessToken,

                @JsonProperty("refresh_token") String refreshToken,

                @JsonProperty("token_type") String tokenType,

                @JsonProperty("expires_in") Long expiresIn,

                @JsonProperty("user") UserAuthResponse user) implements IAuthTokenResponse {

        /**
         * Nested DTO for user authentication information
         * 
         * Immutable response object containing minimal user details.
         * Implements IUserAuthResponse interface.
         * 
         * @param id              User unique identifier
         * @param username        Username for login
         * @param email           Email address
         * @param fullName        User's full name
         * @param phone           Phone number
         * @param avatarUrl       Avatar/profile picture URL
         * @param isEmailVerified Email verification status
         * @param createdAt       Account creation timestamp
         */
        public record UserAuthResponse(
                        String id,
                        String username,
                        String email,
                        String fullName,
                        String phone,
                        String avatarUrl,
                        @JsonProperty("is_email_verified") boolean isEmailVerified,
                        @JsonProperty("created_at") LocalDateTime createdAt) implements IUserAuthResponse {
        }
}
