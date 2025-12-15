package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.ActivityLogSchema;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogSchema, Long> {

    List<ActivityLogSchema> findByTenantId(UUID tenantId);

    List<ActivityLogSchema> findByUserId(UUID userId);
}
