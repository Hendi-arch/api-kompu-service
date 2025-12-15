package com.kompu.api.infrastructure.auth.dto;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kompu.api.usecase.auth.dto.ISignUpRequest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for comprehensive user registration request.
 * 
 * Immutable request object for sign-up operations with multi-tenant support.
 * Implements the framework-independent ISignUpRequest interface.
 * Uses Jakarta validation annotations for input validation.
 * 
 * Maps to database entities:
 * - app.users: username, email, password_hash, full_name, phone, avatar_url
 * - app.members: member_code, full_name, email, phone, address, metadata
 * - app.tenants: name, code, status, metadata
 * - app.user_roles: role assignment
 * 
 * @param username        the username (3-50 characters, alphanumeric +
 *                        underscore)
 * @param email           the email address (must be valid format, unique per
 *                        tenant)
 * @param password        the password (8-100 characters)
 * @param confirmPassword the password confirmation (must match password)
 * @param fullName        the user's full name (1-100 characters)
 * @param phone           the phone number (optional, matched against
 *                        app.users.phone)
 * @param avatarUrl       the avatar/profile picture URL (optional, matched
 *                        against app.users.avatar_url)
 * @param tenantId        the tenant ID (optional, null to create new tenant)
 * @param tenantName      the new tenant name (optional, used if tenantId is
 *                        null)
 * @param tenantCode      the new tenant code (optional, used if tenantId is
 *                        null, defaults to username)
 * @param tenantMetadata  the tenant metadata JSON (optional, stored in
 *                        app.tenants.metadata)
 * @param address         the physical address (optional, matched against
 *                        app.members.address)
 */
public record SignUpRequest(
        // ===== Required User Account Fields =====
        @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters") String password,

        @NotBlank(message = "Password confirmation is required") String confirmPassword,

        @NotBlank(message = "Full name is required") @Size(min = 1, max = 100, message = "Full name must be between 1 and 100 characters") String fullName,

        // ===== Optional User Account Fields =====
        @JsonProperty("phone") @Size(max = 20, message = "Phone number must not exceed 20 characters") String phone,

        @JsonProperty("avatar_url") @Size(max = 500, message = "Avatar URL must not exceed 500 characters") String avatarUrl,

        // ===== Tenant Context Fields =====
        @JsonProperty("theme_id") UUID themeId,

        @JsonProperty("tenant_name") @Size(max = 255, message = "Tenant name must not exceed 255 characters") String tenantName,

        @JsonProperty("tenant_code") @Size(max = 50, message = "Tenant code must not exceed 50 characters") @Pattern(regexp = "^[a-z0-9_-]*$", message = "Tenant code can only contain lowercase letters, numbers, hyphens, and underscores") String tenantCode,

        @JsonProperty("tenant_metadata") @NotEmpty Map<String, Object> tenantMetadata,

        @JsonProperty("plan_name") @Size(max = 50, message = "Plan name must not exceed 50 characters") String planName,

        // ===== Member/Employee Fields =====
        @JsonProperty("address") @Size(max = 500, message = "Address must not exceed 500 characters") String address,

        // ===== Registration Tracking Fields =====
        @JsonProperty("registration_type") @Size(max = 50, message = "Registration type must not exceed 50 characters") String registrationType,

        @JsonProperty("ip_address") @Size(max = 45, message = "IP address must not exceed 45 characters") String ipAddress,

        @JsonProperty("user_agent") @Size(max = 500, message = "User agent must not exceed 500 characters") String userAgent,

        @JsonProperty("terms_accepted_at") Long termsAcceptedAt,

        @JsonProperty("privacy_accepted_at") Long privacyAcceptedAt,

        @JsonProperty("registration_source") @Size(max = 50, message = "Registration source must not exceed 50 characters") String registrationSource)
        implements ISignUpRequest {
}
