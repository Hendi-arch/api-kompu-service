package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.SubscriptionInvoiceSchema;

@Repository
public interface SubscriptionInvoiceRepository extends JpaRepository<SubscriptionInvoiceSchema, UUID> {

    List<SubscriptionInvoiceSchema> findByTenantId(UUID tenantId);

    Optional<SubscriptionInvoiceSchema> findByTenantIdAndInvoiceNumber(UUID tenantId, String invoiceNumber);
}
