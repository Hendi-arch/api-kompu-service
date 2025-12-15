package com.kompu.api.infrastructure.config.db.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.TenantSchema;

@Repository
public interface TenantRepository extends JpaRepository<TenantSchema, UUID> {

    Optional<TenantSchema> findByCode(String code);

    // Using lower case for case-insensitive search if needed, though Schema uses
    // exact match usually
    // Migration has: CREATE INDEX IF NOT EXISTS idx_tenants_code ON
    // app.tenants(lower(code));
    // So we might want a query for case insensitive
    Optional<TenantSchema> findByCodeIgnoreCase(String code);

    Optional<TenantSchema> findByName(String name);

    java.util.List<TenantSchema> findByStatus(String status);
}
