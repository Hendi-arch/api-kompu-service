package com.kompu.api.entity.tenant.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kompu.api.entity.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * TenantModel represents a multi-tenant organization.
 * 
 * This model encapsulates all business logic and state related to a tenant
 * (organization/cooperative).
 * It is completely framework-agnostic and contains no Spring dependencies.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TenantModel extends AbstractEntity<UUID> {

    private UUID id;

    /**
     * Human-readable name of the tenant (e.g., "Koperasi Maju Jaya")
     */
    private String name;

    /**
     * Short code for the tenant (e.g., "kmj", "kj001")
     * Used in domain names and API paths
     */
    private String code;

    /**
     * Current status of the tenant: active | suspended | archived
     */
    private String status;

    /**
     * Additional metadata stored as JSON (configuration, settings, etc.)
     */
    @Builder.Default
    private String metadata = "{}";

    /**
     * ID of the selected dashboard theme
     */
    private UUID themeId;

    /**
     * Tenant founder/owner user ID
     */
    private UUID founderUserId;

    /**
     * Timestamp when the tenant was created
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the tenant was last updated
     */
    private LocalDateTime updatedAt;

    /**
     * User ID who created the tenant
     */
    private String createdBy;

    /**
     * User ID who last updated the tenant
     */
    private String updatedBy;

    /**
     * Soft delete timestamp - null means active
     */
    @JsonIgnore
    private LocalDateTime deletedAt;

    /**
     * Check if the tenant is active
     * 
     * @return true if status is 'active' and not soft-deleted
     */
    public boolean isActive() {
        return "active".equals(this.status) && this.deletedAt == null;
    }

    /**
     * Check if the tenant is suspended
     * 
     * @return true if status is 'suspended'
     */
    public boolean isSuspended() {
        return "suspended".equals(this.status);
    }

    /**
     * Check if the tenant is archived
     * 
     * @return true if status is 'archived'
     */
    public boolean isArchived() {
        return "archived".equals(this.status);
    }

    /**
     * Activate the tenant
     */
    public void activate() {
        this.status = "active";
        this.deletedAt = null;
    }

    /**
     * Suspend the tenant
     */
    public void suspend() {
        this.status = "suspended";
    }

    /**
     * Archive the tenant (soft delete)
     */
    public void archive() {
        this.status = "archived";
        this.deletedAt = LocalDateTime.now();
    }
}
