package com.kompu.api.usecase.auth.dto;

import java.util.Map;
import java.util.UUID;

/**
 * DTO interface for user registration request
 * 
 * Framework-independent contract for sign-up operations.
 * This interface is defined in the Use Case layer and should not have any
 * Spring or validation framework dependencies.
 * 
 * Implementations of this interface should be in the Infrastructure layer
 * with proper validation annotations.
 * 
 * Maps to schema entities:
 * - app.users (username, email, password_hash, full_name, phone, avatar_url)
 * - app.members (member_code, full_name, email, phone, address, joined_at)
 * - app.tenants (name, code, status, metadata)
 * - app.roles (assigned via role name)
 * - app.permissions (inherited from assigned role)
 */
public interface ISignUpRequest {

    // ===== User Account Fields (from app.users) =====

    /**
     * Email address (unique per tenant)
     */
    String email();

    /**
     * Plain-text password (will be hashed)
     */
    String password();

    /**
     * Password confirmation (must match password)
     */
    String confirmPassword();

    /**
     * Full name of the user
     */
    String fullName();

    /**
     * Optional phone number of the user
     */
    String phone();

    /**
     * Optional avatar URL or file path for profile picture
     * For multipart uploads, provide URL after file is stored
     */
    String avatarUrl();

    // ===== Tenant Context =====

    /**
     * Theme ID for the tenant
     */
    UUID themeId();

    /**
     * Optional tenant name (used when creating new tenant).
     * If null, generated from username or other logic.
     */
    String tenantName();

    /**
     * Optional tenant code/slug (used when creating new tenant).
     * If null, generated from username (lowercase).
     */
    String tenantCode();

    /**
     * Optional tenant metadata as JSON string.
     * Stored in app.tenants.metadata JSONB field.
     */
    Map<String, Object> tenantMetadata();

    /**
     * Optional subscription plan name (e.g., 'BASIC_MONTHLY').
     * If null, defaults to 'BASIC_MONTHLY'.
     */
    String planName();

    // ===== Member/Employee Fields (from app.members) =====

    /**
     * Optional physical address of the member
     */
    String address();

    // ===== Registration Tracking Fields =====

    /**
     * Registration type (e.g., 'koperasi', 'non-koperasi')
     * Used for compliance and analytics tracking
     */
    String registrationType();

    /**
     * IP address of the registration request
     * Used for security audit trail
     */
    String ipAddress();

    /**
     * User agent string from the client browser
     * Stored for device tracking and security analysis
     */
    String userAgent();

    /**
     * Timestamp (epoch milliseconds) when terms were accepted
     * Used for legal compliance tracking
     */
    Long termsAcceptedAt();

    /**
     * Timestamp (epoch milliseconds) when privacy policy was accepted
     * Used for legal compliance tracking
     */
    Long privacyAcceptedAt();

    /**
     * Registration source channel (e.g., 'web', 'api', 'mobile', 'manual')
     * Helps track which platform initiated the registration
     */
    String registrationSource();

}
