package com.kompu.api.entity.subscription.model;

import java.time.LocalDate;
import java.util.UUID;

import com.kompu.api.entity.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * TenantSubscriptionModel - Domain model for active subscription per tenant
 * Tracks which subscription plan each tenant has and its status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TenantSubscriptionModel extends AbstractEntity<UUID> {

    private UUID id;
    private UUID tenantId; // FK to tenant
    private UUID planId; // FK to subscription plan
    private LocalDate subscriptionStartDate; // ISO date format
    private LocalDate subscriptionEndDate; // ISO date format (nullable)
    @Builder.Default
    private String status = "active"; // 'trial', 'active', 'suspended', 'cancelled', 'expired'
    @Builder.Default
    private Boolean autoRenew = true;
    private java.time.LocalDateTime trialEndsAt;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

}
