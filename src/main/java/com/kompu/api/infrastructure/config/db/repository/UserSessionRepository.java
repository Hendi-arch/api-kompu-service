package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.UserSessionSchema;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSessionSchema, UUID> {

    List<UserSessionSchema> findByUserId(UUID userId);

    List<UserSessionSchema> findByUserIdAndIsActiveTrue(UUID userId);

    List<UserSessionSchema> findByTenantId(UUID tenantId);

    Optional<UserSessionSchema> findByIdAndIsActiveTrue(UUID id);

}
