package com.kompu.api.infrastructure.config.db.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.SupplierProductPriceSchema;

@Repository
public interface SupplierProductPriceRepository extends JpaRepository<SupplierProductPriceSchema, UUID> {

    List<SupplierProductPriceSchema> findByTenantId(UUID tenantId);

    List<SupplierProductPriceSchema> findBySupplierId(UUID supplierId);

    List<SupplierProductPriceSchema> findByProductId(UUID productId);

    List<SupplierProductPriceSchema> findByProductIdAndUnitPriceLessThanEqual(UUID productId, BigDecimal price);
}
