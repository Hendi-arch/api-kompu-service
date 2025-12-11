package com.kompu.api.entity.featureflag.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.featureflag.model.FeatureFlagModel;

/**
 * FeatureFlagGateway defines the contract for all feature flag data access
 * operations.
 * 
 * This interface abstracts the underlying persistence mechanism from business
 * logic.
 * Implementations must handle both global and tenant-specific flags.
 * 
 * Gateway Pattern: Allows the entity layer to remain independent of
 * infrastructure details.
 */
public interface FeatureFlagGateway {

    /**
     * Create a new feature flag
     * 
     * @param flagModel the flag to create
     * @return the created flag with ID assigned
     */
    FeatureFlagModel create(FeatureFlagModel flagModel);

    /**
     * Update an existing feature flag
     * 
     * @param flagModel the flag with updated values
     * @return the updated flag
     */
    FeatureFlagModel update(FeatureFlagModel flagModel);

    /**
     * Delete a feature flag by ID
     * 
     * @param id the flag ID
     */
    void delete(UUID id);

    /**
     * Find a feature flag by ID
     * 
     * @param id the flag ID
     * @return Optional containing the flag, or empty if not found
     */
    Optional<FeatureFlagModel> findById(UUID id);

    /**
     * Find a global feature flag by key
     * 
     * @param key the feature flag key
     * @return Optional containing the flag, or empty if not found
     */
    Optional<FeatureFlagModel> findGlobalByKey(String key);

    /**
     * Find a tenant-specific feature flag by key
     * 
     * @param tenantId the tenant ID
     * @param key      the feature flag key
     * @return Optional containing the flag, or empty if not found
     */
    Optional<FeatureFlagModel> findByTenantIdAndKey(UUID tenantId, String key);

    /**
     * Find all global feature flags
     * 
     * @return list of all global flags
     */
    List<FeatureFlagModel> findAllGlobal();

    /**
     * Find all feature flags for a specific tenant (includes tenant-specific and
     * global)
     * 
     * @param tenantId the tenant ID
     * @return list of all flags applicable to the tenant
     */
    List<FeatureFlagModel> findByTenantId(UUID tenantId);

    /**
     * Find only tenant-specific feature flags (excluding global)
     * 
     * @param tenantId the tenant ID
     * @return list of tenant-specific flags
     */
    List<FeatureFlagModel> findTenantSpecificByTenantId(UUID tenantId);

    /**
     * Check if a feature is enabled for a tenant
     * 
     * Resolves with tenant-specific flag first, falls back to global flag
     * 
     * @param tenantId the tenant ID
     * @param key      the feature flag key
     * @return true if the feature is enabled, false otherwise
     */
    boolean isFeatureEnabled(UUID tenantId, String key);

    /**
     * Check if a global feature is enabled
     * 
     * @param key the feature flag key
     * @return true if the feature is enabled, false otherwise
     */
    boolean isGlobalFeatureEnabled(String key);
}
