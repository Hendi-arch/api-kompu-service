package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.UserRoleId;
import com.kompu.api.infrastructure.config.db.schema.UserRoleSchema;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleSchema, UserRoleId> {

    List<UserRoleSchema> findByUserId(UUID userId);

    List<UserRoleSchema> findByRoleId(UUID roleId);

    boolean existsByUserIdAndRoleId(UUID userId, UUID roleId);

}
