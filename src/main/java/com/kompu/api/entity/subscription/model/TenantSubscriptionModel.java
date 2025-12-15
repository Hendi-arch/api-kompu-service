package com.kompu.api.entity.subscription.model;

import java.time.Instant;
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
    private String subscriptionStartDate; // ISO date format
    private String subscriptionEndDate; // ISO date format (nullable)
    @Builder.Default
    private String status = "active"; // 'trial', 'active', 'suspended', 'cancelled', 'expired'
    @Builder.Default
    private Boolean autoRenew = true;
    private Instant trialEndsAt;
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

}
