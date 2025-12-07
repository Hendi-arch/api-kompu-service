package com.kompu.api.infrastructure.config.db.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kompu.api.infrastructure.config.db.schema.UserTokenSchema;

public interface UserTokenRepository extends JpaRepository<UserTokenSchema, Long> {
    // Add custom query methods if needed

    void deleteByToken(String token);

    Optional<UserTokenSchema> findByToken(String token);
}