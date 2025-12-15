package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.SubscriptionPlanSchema;

/**
 * SubscriptionPlanRepository - JPA repository for subscription plan queries
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlanSchema, UUID> {

    Optional<SubscriptionPlanSchema> findByNameIgnoreCase(String name);

    @Query("SELECT sp FROM SubscriptionPlanSchema sp WHERE sp.isActive = true ORDER BY sp.price ASC")
    List<SubscriptionPlanSchema> findAllActive();

    @Query("SELECT sp FROM SubscriptionPlanSchema sp WHERE sp.isActive = true AND sp.name IN ('BASIC', 'PRO', 'ENTERPRISE')")
    List<SubscriptionPlanSchema> findDefaultPlans();
}
