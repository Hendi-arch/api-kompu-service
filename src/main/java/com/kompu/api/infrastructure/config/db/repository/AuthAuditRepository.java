package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.AuthAuditSchema;

@Repository
public interface AuthAuditRepository extends JpaRepository<AuthAuditSchema, Long> {

    List<AuthAuditSchema> findByTenantId(UUID tenantId);

    List<AuthAuditSchema> findByUserId(UUID userId);
}
