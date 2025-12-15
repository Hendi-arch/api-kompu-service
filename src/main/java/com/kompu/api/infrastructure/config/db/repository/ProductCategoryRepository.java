package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.ProductCategorySchema;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategorySchema, UUID> {

    List<ProductCategorySchema> findByTenantId(UUID tenantId);

    Optional<ProductCategorySchema> findByTenantIdAndSlug(UUID tenantId, String slug);
}
