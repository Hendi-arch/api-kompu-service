package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.FeatureFlagSchema;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlagSchema, UUID> {

    Optional<FeatureFlagSchema> findByTenantIdAndKey(UUID tenantId, String key);

    List<FeatureFlagSchema> findByTenantId(UUID tenantId);

    List<FeatureFlagSchema> findByTenantIdIsNull();
}
