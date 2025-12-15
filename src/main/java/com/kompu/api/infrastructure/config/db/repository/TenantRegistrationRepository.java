package com.kompu.api.infrastructure.config.db.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.TenantRegistrationSchema;

@Repository
public interface TenantRegistrationRepository extends JpaRepository<TenantRegistrationSchema, UUID> {

    java.util.Optional<TenantRegistrationSchema> findByTenantId(UUID tenantId);

    java.util.Optional<TenantRegistrationSchema> findByEmailUsed(String emailUsed);
}
