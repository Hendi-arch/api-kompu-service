package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.PaymentSchema;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentSchema, UUID> {

    List<PaymentSchema> findByTenantId(UUID tenantId);

    List<PaymentSchema> findByOrderId(UUID orderId);
}
