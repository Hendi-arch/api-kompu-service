package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.LoginLogSchema;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLogSchema, Long> {

    List<LoginLogSchema> findByTenantId(UUID tenantId);

    List<LoginLogSchema> findByUserId(UUID userId);
}
