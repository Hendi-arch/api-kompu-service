package com.kompu.api.entity.tenantdomain.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.tenantdomain.model.TenantDomainModel;

/**
 * TenantDomainGateway defines the contract for all tenant domain data access
 * operations.
 * 
 * This interface abstracts the underlying persistence mechanism from business
 * logic.
 * Implementations must handle domain uniqueness and tenant isolation.
 * 
 * Gateway Pattern: Allows the entity layer to remain independent of
 * infrastructure details.
 */
public interface TenantDomainGateway {

    /**
     * Create a new tenant domain
     * 
     * @param domainModel the domain to create
     * @return the created domain with ID assigned
     */
    TenantDomainModel create(TenantDomainModel domainModel);

    /**
     * Update an existing tenant domain
     * 
     * @param domainModel the domain with updated values
     * @return the updated domain
     */
    TenantDomainModel update(TenantDomainModel domainModel);

    /**
     * Delete a tenant domain by ID
     * 
     * @param id the domain ID
     */
    void delete(UUID id);

    /**
     * Find a tenant domain by ID
     * 
     * @param id the domain ID
     * @return Optional containing the domain, or empty if not found
     */
    Optional<TenantDomainModel> findById(UUID id);

    /**
     * Find a tenant domain by host (domain name)
     * 
     * @param host the hostname/domain
     * @return Optional containing the domain, or empty if not found
     */
    Optional<TenantDomainModel> findByHost(String host);

    /**
     * Find the primary domain for a tenant
     * 
     * @param tenantId the tenant ID
     * @return Optional containing the primary domain, or empty if not found
     */
    Optional<TenantDomainModel> findPrimaryByTenantId(UUID tenantId);

    /**
     * Find all domains for a tenant
     * 
     * @param tenantId the tenant ID
     * @return list of all domains for the tenant
     */
    List<TenantDomainModel> findByTenantId(UUID tenantId);

    /**
     * Find all active domains for a tenant
     * 
     * @param tenantId the tenant ID
     * @return list of active domains for the tenant
     */
    List<TenantDomainModel> findActiveByTenantId(UUID tenantId);

    /**
     * Find all custom domains for a tenant
     * 
     * @param tenantId the tenant ID
     * @return list of custom domains for the tenant
     */
    List<TenantDomainModel> findCustomByTenantId(UUID tenantId);

    /**
     * Check if a domain host exists globally
     * 
     * @param host the hostname
     * @return true if the host is already used, false otherwise
     */
    boolean existsByHost(String host);

    /**
     * Check if a domain exists for a tenant
     * 
     * @param tenantId the tenant ID
     * @param host     the hostname
     * @return true if the domain exists for the tenant, false otherwise
     */
    boolean existsByTenantIdAndHost(UUID tenantId, String host);
}
