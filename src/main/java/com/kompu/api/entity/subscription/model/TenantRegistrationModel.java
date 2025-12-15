package com.kompu.api.entity.subscription.model;

import java.util.UUID;

import com.kompu.api.entity.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * TenantRegistrationModel - Domain model for registration audit trail
 * Tracks registration details for compliance, security, and marketing analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TenantRegistrationModel extends AbstractEntity<UUID> {

    private UUID id;
    private UUID tenantId;
    private String registrationType; // 'koperasi', 'non-koperasi'
    private UUID adminUserId;
    private String emailUsed;
    private String ipAddress;
    private String userAgent;
    private Long termsAcceptedAt;
    private Long privacyAcceptedAt;
    private Long emailVerificationSentAt;
    private Long emailVerifiedAt;
    @Builder.Default
    private String registrationSource = "web"; // 'web', 'api', 'mobile', 'manual'
    private String notes;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

}
