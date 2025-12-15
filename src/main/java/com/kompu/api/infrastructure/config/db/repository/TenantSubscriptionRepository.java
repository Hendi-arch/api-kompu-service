package com.kompu.api.infrastructure.config.db.repository;

import com.kompu.api.infrastructure.config.db.schema.TenantSubscriptionSchema;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * TenantSubscriptionRepository - JPA repository for tenant subscription queries
 */
@Repository
public interface TenantSubscriptionRepository extends JpaRepository<TenantSubscriptionSchema, UUID> {

    Optional<TenantSubscriptionSchema> findByTenantId(UUID tenantId);

    @Query("SELECT ts FROM TenantSubscriptionSchema ts WHERE ts.tenantId = :tenantId AND ts.status = 'active'")
    Optional<TenantSubscriptionSchema> findActiveByTenantId(UUID tenantId);
}
