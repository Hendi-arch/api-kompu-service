package com.kompu.api.entity.featureflag.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kompu.api.entity.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * FeatureFlagModel represents a feature flag for a tenant or global system.
 * 
 * Feature flags are used to control feature availability and behavior:
 * - Global flags (tenant_id = NULL) apply to all tenants
 * - Tenant flags override global defaults for that tenant
 * - Flags can be enabled/disabled dynamically without code changes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FeatureFlagModel extends AbstractEntity<UUID> {

    private UUID id;

    /**
     * The tenant context (NULL for global flags)
     */
    private UUID tenantId;

    /**
     * Unique key for the feature flag (e.g., "payments.enabled",
     * "advanced_reporting")
     */
    private String key;

    /**
     * The flag value stored as JSON (can be boolean, string, object, etc.)
     */
    private String value;

    /**
     * Whether this flag is currently enabled
     */
    private boolean enabled;

    /**
     * Timestamp when the flag was created
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the flag was last updated
     */
    private LocalDateTime updatedAt;

    /**
     * Check if this is a global feature flag
     * 
     * @return true if tenant_id is null
     */
    public boolean isGlobal() {
        return this.tenantId == null;
    }

    /**
     * Check if this is a tenant-specific feature flag
     * 
     * @return true if tenant_id is not null
     */
    public boolean isTenantSpecific() {
        return this.tenantId != null;
    }

    /**
     * Enable this feature flag
     */
    public void enable() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Disable this feature flag
     */
    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Update the flag value
     * 
     * @param newValue the new value
     */
    public void updateValue(String newValue) {
        this.value = newValue;
        this.updatedAt = LocalDateTime.now();
    }
}
