package com.kompu.api.entity.tenantdomain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kompu.api.entity.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * TenantDomainModel represents a domain/hostname associated with a tenant.
 * 
 * Tenants can have multiple domains:
 * - One primary domain (main access point)
 * - Custom domains (white-label, branded)
 * - Sub-domains (regional, department-specific)
 * 
 * Example:
 * - Primary: koperasi1.kompu.id
 * - Custom: www.koperasi-maju.com
 * - Secondary: branch.koperasi-maju.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TenantDomainModel extends AbstractEntity<UUID> {

    private UUID id;

    /**
     * The tenant this domain belongs to
     */
    private UUID tenantId;

    /**
     * The domain host/hostname (e.g., "koperasi1.kompu.id", "www.mycompany.com")
     */
    private String host;

    /**
     * Whether this is the primary domain for the tenant
     */
    private boolean primary;

    /**
     * Whether this is a custom domain (vs. platform-provided subdomain)
     */
    private boolean custom;

    /**
     * Whether HTTPS is enabled for this domain
     */
    private boolean httpsEnabled;

    /**
     * The TLS certificate provider (e.g., "cloudflare", "letsencrypt", "custom")
     */
    private String tlsProvider;

    /**
     * Timestamp when the domain was created
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the domain was last updated
     */
    private LocalDateTime updatedAt;

    /**
     * Soft delete timestamp - null means active
     */
    private LocalDateTime deletedAt;

    /**
     * Check if this domain is active (not soft-deleted)
     * 
     * @return true if deletedAt is null
     */
    public boolean isActive() {
        return this.deletedAt == null;
    }

    /**
     * Mark this domain as the primary domain for the tenant
     */
    public void setPrimaryDomain() {
        this.primary = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark this domain as secondary
     */
    public void setSecondaryDomain() {
        this.primary = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Enable HTTPS for this domain
     */
    public void enableHttps() {
        this.httpsEnabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Disable HTTPS for this domain (not recommended for production)
     */
    public void disableHttps() {
        this.httpsEnabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Soft-delete this domain
     */
    public void deactivate() {
        this.deletedAt = LocalDateTime.now();
    }
}
