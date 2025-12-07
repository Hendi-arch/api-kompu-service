package com.kompu.api.infrastructure.config.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kompu.api.infrastructure.config.db.schema.UserRoleSchema;

public interface UserRoleRepository extends JpaRepository<UserRoleSchema, Long> {
    // Add custom query methods if needed
}
