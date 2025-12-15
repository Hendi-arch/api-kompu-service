package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.OrderSchema;

@Repository
public interface OrderRepository extends JpaRepository<OrderSchema, UUID> {

    List<OrderSchema> findByTenantId(UUID tenantId);

    Optional<OrderSchema> findByTenantIdAndOrderNumber(UUID tenantId, String orderNumber);
}
