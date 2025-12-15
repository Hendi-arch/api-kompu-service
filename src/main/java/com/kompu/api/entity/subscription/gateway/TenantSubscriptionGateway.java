package com.kompu.api.entity.subscription.gateway;

import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.subscription.model.TenantSubscriptionModel;

/**
 * TenantSubscriptionGateway - Gateway interface for tenant subscription data
 * access
 * Defines contract for managing active subscriptions per tenant
 */
public interface TenantSubscriptionGateway {

    /**
     * Find subscription by tenant ID (tenants have exactly one active subscription)
     */
    Optional<TenantSubscriptionModel> findByTenantId(UUID tenantId);

    /**
     * Find subscription by ID
     */
    Optional<TenantSubscriptionModel> findById(UUID id);

    /**
     * Save new tenant subscription
     */
    TenantSubscriptionModel save(TenantSubscriptionModel model);

    /**
     * Update existing tenant subscription
     */
    TenantSubscriptionModel update(TenantSubscriptionModel model);

    /**
     * Delete tenant subscription (soft delete)
     */
    void delete(UUID id);

    /**
     * Change tenant's subscription plan
     */
    TenantSubscriptionModel changePlan(UUID tenantId, UUID newPlanId);
}
