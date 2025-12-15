package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.UserSchema;

@Repository
public interface UserRepository extends JpaRepository<UserSchema, UUID> {

    Optional<UserSchema> findByEmail(String email);

    List<UserSchema> findByTenantId(UUID tenantId);

    @Query("SELECT u FROM UserSchema u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.email = :email")
    Optional<UserSchema> findByEmailWithRolesAndPermissions(@Param("email") String email);

    @Query("SELECT u FROM UserSchema u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.id = :id")
    Optional<UserSchema> findByIdWithRolesAndPermissions(@Param("id") UUID id);

}