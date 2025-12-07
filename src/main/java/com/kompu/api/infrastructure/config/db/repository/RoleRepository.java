package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.RoleSchema;

@Repository
public interface RoleRepository extends JpaRepository<RoleSchema, UUID> {

    Optional<RoleSchema> findByNameAndTenantId(String name, UUID tenantId);

    List<RoleSchema> findByTenantId(UUID tenantId);

}
