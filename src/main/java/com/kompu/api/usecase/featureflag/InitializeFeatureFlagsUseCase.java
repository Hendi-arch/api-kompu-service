package com.kompu.api.usecase.featureflag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.kompu.api.entity.featureflag.model.FeatureFlagModel;
import com.kompu.api.entity.featureflag.gateway.FeatureFlagGateway;

/**
 * InitializeFeatureFlagsUseCase orchestrates the initialization of feature
 * flags for a new tenant.
 * 
 * This use case handles:
 * 1. Creating default feature flags for a tenant
 * 2. Copying global flags as tenant overrides (optional)
 * 3. Setting initial enabled/disabled states
 * 4. Persisting all flags via the gateway
 * 
 * Responsibilities:
 * - Initialize standard feature flags (payments, reporting, etc.)
 * - Set sensible defaults based on tenant tier/plan
 * - Create audit trail for flag changes
 * - Batch create multiple flags efficiently
 * 
 * This class is completely framework-agnostic and contains pure business logic.
 */
public class InitializeFeatureFlagsUseCase {

    private final FeatureFlagGateway featureFlagGateway;

    /**
     * Constructor with gateway dependency injection
     * 
     * @param featureFlagGateway the gateway for feature flag persistence
     */
    public InitializeFeatureFlagsUseCase(FeatureFlagGateway featureFlagGateway) {
        this.featureFlagGateway = featureFlagGateway;
    }

    /**
     * Initialize default feature flags for a new tenant.
     * 
     * Creates standard feature flags with default configurations:
     * - Core features enabled (dashboard, basic reporting)
     * - Premium features disabled (advanced analytics, custom integrations)
     * - Payment processing enabled
     * 
     * @param tenantId the tenant ID
     * @return list of created feature flags
     */
    public List<FeatureFlagModel> initializeTenantFlags(UUID tenantId) {
        List<FeatureFlagModel> flags = new ArrayList<>();

        // Core features (enabled by default)
        flags.add(createFlag(tenantId, "dashboard.enabled", "true", true));
        flags.add(createFlag(tenantId, "basic_reporting.enabled", "true", true));
        flags.add(createFlag(tenantId, "user_management.enabled", "true", true));
        flags.add(createFlag(tenantId, "member_management.enabled", "true", true));

        // Payment features (enabled by default)
        flags.add(createFlag(tenantId, "payments.enabled", "true", true));
        flags.add(createFlag(tenantId, "online_payments.enabled", "false", false));

        // Business features (enabled by default)
        flags.add(createFlag(tenantId, "inventory_management.enabled", "true", true));
        flags.add(createFlag(tenantId, "order_management.enabled", "true", true));

        // Advanced features (disabled by default)
        flags.add(createFlag(tenantId, "advanced_analytics.enabled", "false", false));
        flags.add(createFlag(tenantId, "api_access.enabled", "false", false));
        flags.add(createFlag(tenantId, "webhook_integration.enabled", "false", false));

        // Financial features (disabled by default)
        flags.add(createFlag(tenantId, "savings_products.enabled", "false", false));
        flags.add(createFlag(tenantId, "loan_products.enabled", "false", false));

        // Bulk operations (disabled by default - requires approval)
        flags.add(createFlag(tenantId, "bulk_import.enabled", "false", false));
        flags.add(createFlag(tenantId, "bulk_export.enabled", "true", true));

        // Persist all flags
        for (FeatureFlagModel flag : flags) {
            featureFlagGateway.create(flag);
        }

        return flags;
    }

    /**
     * Initialize feature flags with a specific configuration.
     * 
     * @param tenantId           the tenant ID
     * @param flagConfigurations map of flag keys to enabled status
     * @return list of created feature flags
     */
    public List<FeatureFlagModel> initializeTenantFlagsWithConfig(
            UUID tenantId,
            java.util.Map<String, Boolean> flagConfigurations) {

        List<FeatureFlagModel> flags = new ArrayList<>();

        for (String key : flagConfigurations.keySet()) {
            boolean enabled = flagConfigurations.get(key);
            FeatureFlagModel flag = createFlag(tenantId, key, String.valueOf(enabled), enabled);
            flags.add(flag);
            featureFlagGateway.create(flag);
        }

        return flags;
    }

    /**
     * Create a feature flag for a tenant based on a global template flag.
     * 
     * @param tenantId        the tenant ID
     * @param globalFlagKey   the global flag key to use as template
     * @param overrideEnabled optionally override the enabled status
     * @return the created feature flag
     */
    public FeatureFlagModel copyGlobalFlagToTenant(
            UUID tenantId,
            String globalFlagKey,
            Boolean overrideEnabled) {

        // Create a tenant-specific flag based on the global flag
        boolean enabled = overrideEnabled != null ? overrideEnabled : true;
        FeatureFlagModel flag = createFlag(tenantId, globalFlagKey, String.valueOf(enabled), enabled);

        return featureFlagGateway.create(flag);
    }

    /**
     * Enable a feature flag for a tenant.
     * 
     * @param tenantId the tenant ID
     * @param key      the feature flag key
     * @return the updated flag
     */
    public FeatureFlagModel enableFeature(UUID tenantId, String key) {
        var flagOpt = featureFlagGateway.findByTenantIdAndKey(tenantId, key);

        if (flagOpt.isPresent()) {
            FeatureFlagModel flag = flagOpt.get();
            flag.enable();
            return featureFlagGateway.update(flag);
        }

        // Create new flag if not exists
        return featureFlagGateway.create(createFlag(tenantId, key, "true", true));
    }

    /**
     * Disable a feature flag for a tenant.
     * 
     * @param tenantId the tenant ID
     * @param key      the feature flag key
     * @return the updated flag
     */
    public FeatureFlagModel disableFeature(UUID tenantId, String key) {
        var flagOpt = featureFlagGateway.findByTenantIdAndKey(tenantId, key);

        if (flagOpt.isPresent()) {
            FeatureFlagModel flag = flagOpt.get();
            flag.disable();
            return featureFlagGateway.update(flag);
        }

        // Create new flag if not exists (but disabled)
        return featureFlagGateway.create(createFlag(tenantId, key, "false", false));
    }

    /**
     * Helper method to create a feature flag model
     * 
     * @param tenantId the tenant ID
     * @param key      the flag key
     * @param value    the flag value
     * @param enabled  whether the flag is enabled
     * @return a new FeatureFlagModel
     */
    private FeatureFlagModel createFlag(UUID tenantId, String key, String value, boolean enabled) {
        return FeatureFlagModel.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .key(key)
                .value(value)
                .enabled(enabled)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
