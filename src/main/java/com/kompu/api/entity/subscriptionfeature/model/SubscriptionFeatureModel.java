package com.kompu.api.entity.subscriptionfeature.model;

import java.util.UUID;

import com.kompu.api.entity.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * SubscriptionFeatureModel - Domain model for subscription features
 * Represents feature flags that can be included in subscription plans
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SubscriptionFeatureModel extends AbstractEntity<UUID> {

    private UUID id;
    private String featureKey; // e.g., 'feature.member_portal'
    private String displayName;
    private String description;
    private String category; // 'administration', 'member', 'supply', 'finance', 'reporting'
}
