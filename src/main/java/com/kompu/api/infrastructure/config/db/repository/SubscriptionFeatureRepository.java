package com.kompu.api.infrastructure.config.db.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.SubscriptionFeatureSchema;

@Repository
public interface SubscriptionFeatureRepository extends JpaRepository<SubscriptionFeatureSchema, UUID> {

    Optional<SubscriptionFeatureSchema> findByFeatureKey(String featureKey);
}
