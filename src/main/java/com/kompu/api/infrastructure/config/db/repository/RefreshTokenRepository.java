package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.RefreshTokenSchema;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenSchema, UUID> {

    List<RefreshTokenSchema> findByUserId(UUID userId);

    List<RefreshTokenSchema> findBySessionId(UUID sessionId);

    Optional<RefreshTokenSchema> findByTokenHash(String tokenHash);

    List<RefreshTokenSchema> findByUserIdAndRevokedAtIsNull(UUID userId);

}
