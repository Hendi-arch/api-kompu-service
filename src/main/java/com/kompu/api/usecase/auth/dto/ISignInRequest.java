package com.kompu.api.usecase.auth.dto;

import java.util.UUID;

/**
 * DTO interface for user authentication request.
 * 
 * Framework-independent contract for sign-in operations.
 * This interface is defined in the Use Case layer and should not have any
 * Spring or validation framework dependencies.
 * 
 * Implementations of this interface should be in the Infrastructure layer
 * with proper validation annotations.
 * 
 * Maps to schema entities:
 * - app.users (find by username, verify password_hash)
 * - app.user_sessions (track login session with ip, user_agent)
 * - app.user_roles (retrieve user permissions)
 * - app.login_log (audit trail)
 */
public interface ISignInRequest {

    /**
     * Username for authentication lookup.
     * Used to find user in app.users table (unique per tenant).
     */
    String username();

    /**
     * Plain-text password to verify against password_hash.
     * Validated using BCrypt comparison in ValidateUserCredentialsUseCase.
     */
    String password();

    /**
     * Optional tenant ID context for sign-in.
     * If provided, restricts user lookup to specific tenant.
     * If null, looks up user globally (for multi-tenant support).
     * 
     * @return tenant ID, or null for global lookup
     */
    UUID tenantId();

    /**
     * Optional client IP address for session tracking and audit logging.
     * Captured from HTTP request headers or RemoteAddr.
     * Stored in app.user_sessions.ip and app.login_log.ip.
     * 
     * @return IP address string, or null if not provided
     */
    String clientIpAddress();

    /**
     * Optional user agent string for device identification.
     * Captured from User-Agent HTTP header.
     * Stored in app.user_sessions.user_agent and app.login_log.user_agent.
     * 
     * @return user agent string, or null if not provided
     */
    String userAgent();

}
