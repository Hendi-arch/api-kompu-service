package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.InventorySchema;

@Repository
public interface InventoryRepository extends JpaRepository<InventorySchema, UUID> {

    List<InventorySchema> findByTenantId(UUID tenantId);

    Optional<InventorySchema> findByTenantIdAndProductIdAndLocation(UUID tenantId, UUID productId, String location);

    List<InventorySchema> findByTenantIdAndProductId(UUID tenantId, UUID productId);
}
