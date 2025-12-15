package com.kompu.api.entity.subscription.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.subscription.model.SubscriptionPlanModel;

/**
 * SubscriptionPlanGateway - Gateway interface for subscription plan data access
 * Defines contract for CRUD operations on subscription plans
 */
public interface SubscriptionPlanGateway {

    /**
     * Find subscription plan by ID
     */
    Optional<SubscriptionPlanModel> findById(UUID id);

    /**
     * Find subscription plan by name (BASIC, PRO, ENTERPRISE)
     */
    Optional<SubscriptionPlanModel> findByName(String name);

    /**
     * Find all active subscription plans
     */
    List<SubscriptionPlanModel> findAllActive();

    /**
     * Find all subscription plans (active and inactive)
     */
    List<SubscriptionPlanModel> findAll();

    /**
     * Save subscription plan
     */
    SubscriptionPlanModel save(SubscriptionPlanModel model);

    /**
     * Update subscription plan
     */
    SubscriptionPlanModel update(SubscriptionPlanModel model);

    /**
     * Delete subscription plan by ID (soft delete if supported)
     */
    void delete(UUID id);
}
