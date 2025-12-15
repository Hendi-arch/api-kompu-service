package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.TenantDomainSchema;

@Repository
public interface TenantDomainRepository extends JpaRepository<TenantDomainSchema, UUID> {

    Optional<TenantDomainSchema> findByHost(String host);

    List<TenantDomainSchema> findByTenantId(UUID tenantId);

    // Find primary domain for a tenant
    // Find primary domain for a tenant
    Optional<TenantDomainSchema> findByTenantIdAndIsPrimaryTrue(UUID tenantId);

    List<TenantDomainSchema> findByTenantIdAndIsCustomTrue(UUID tenantId);

    boolean existsByHost(String host);

    boolean existsByTenantIdAndHost(UUID tenantId, String host);
}
