package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.ProductSchema;

@Repository
public interface ProductRepository extends JpaRepository<ProductSchema, UUID> {

    List<ProductSchema> findByTenantId(UUID tenantId);

    Optional<ProductSchema> findByTenantIdAndSku(UUID tenantId, String sku);

    List<ProductSchema> findByTenantIdAndCategoryId(UUID tenantId, UUID categoryId);
}
