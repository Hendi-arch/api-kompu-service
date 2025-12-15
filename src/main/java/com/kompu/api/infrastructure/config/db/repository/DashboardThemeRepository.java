package com.kompu.api.infrastructure.config.db.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.DashboardThemeSchema;

@Repository
public interface DashboardThemeRepository extends JpaRepository<DashboardThemeSchema, UUID> {

    Optional<DashboardThemeSchema> findByName(String name);
}
