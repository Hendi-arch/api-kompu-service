package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.RolePermissionId;
import com.kompu.api.infrastructure.config.db.schema.RolePermissionSchema;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermissionSchema, RolePermissionId> {

    RolePermissionSchema findByRoleId(UUID roleId);

    List<RolePermissionSchema> findByPermissionId(UUID permissionId);

    boolean existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

}
