package com.kompu.api.infrastructure.config.db.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.AppConfigSchema;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfigSchema, UUID> {

    Optional<AppConfigSchema> findByConfigKey(String configKey);

}
