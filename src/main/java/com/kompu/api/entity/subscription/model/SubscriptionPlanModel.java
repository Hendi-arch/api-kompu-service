package com.kompu.api.entity.subscription.model;

import java.math.BigDecimal;
import java.util.UUID;

import com.kompu.api.entity.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * SubscriptionPlanModel - Domain model for subscription plan tiers
 * Represents BASIC, PRO, ENTERPRISE plans with feature access and pricing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SubscriptionPlanModel extends AbstractEntity<UUID> {

    private UUID id;
    private String name; // 'BASIC', 'PRO', 'ENTERPRISE'
    private BigDecimal price;
    private String currency; // 'IDR'
    private String billingPeriod; // 'monthly', 'yearly'
    private Integer maxUsers; // -1 = unlimited
    private Integer maxUnitUsaha;
    private String description;
    @Builder.Default
    private Boolean isActive = true;

}
