package com.kompu.api.infrastructure.auth.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kompu.api.usecase.auth.dto.ISignInRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for comprehensive user authentication request.
 * 
 * Immutable request object for sign-in operations with multi-tenant and device
 * tracking support.
 * Implements the framework-independent ISignInRequest interface.
 * Uses Jakarta validation annotations for input validation.
 * 
 * Maps to database entities:
 * - app.users: username lookup, password_hash verification
 * - app.user_sessions: session creation with ip, user_agent
 * - app.user_roles: role/permission retrieval
 * - app.login_log: audit trail of login attempts
 * 
 * @param username        the username for authentication (required)
 * @param password        the plain-text password (required)
 * @param tenantId        the tenant context (optional, for multi-tenant
 *                        isolation)
 * @param clientIpAddress the client IP address (optional, for session tracking)
 * @param userAgent       the User-Agent header (optional, for device
 *                        identification)
 */
public record SignInRequest(
        @NotBlank(message = "Username is required") @Size(min = 1, max = 50, message = "Username must be between 1 and 50 characters") String username,

        @NotBlank(message = "Password is required") @Size(min = 1, max = 100, message = "Password must be between 1 and 100 characters") String password,

        @JsonProperty("tenant_id") UUID tenantId,

        @JsonProperty("client_ip_address") @Size(max = 45, message = "IP address must not exceed 45 characters (IPv6)") String clientIpAddress,

        @JsonProperty("user_agent") @Size(max = 500, message = "User agent must not exceed 500 characters") String userAgent)
        implements ISignInRequest {

}
