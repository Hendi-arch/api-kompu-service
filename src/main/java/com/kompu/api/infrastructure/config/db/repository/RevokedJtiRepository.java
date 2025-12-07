package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.RevokedJtiSchema;

@Repository
public interface RevokedJtiRepository extends JpaRepository<RevokedJtiSchema, UUID> {

    Optional<RevokedJtiSchema> findByJti(UUID jti);

    List<RevokedJtiSchema> findByUserId(UUID userId);

}
