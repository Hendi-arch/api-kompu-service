package com.kompu.api.entity.tenant.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.tenant.exception.TenantNotFoundException;
import com.kompu.api.entity.tenant.model.TenantModel;

/**
 * TenantGateway defines the contract for all tenant data access operations.
 * 
 * This interface abstracts the underlying persistence mechanism (database,
 * cache, etc.)
 * from the business logic. Implementations must handle multi-tenant isolation.
 * 
 * Gateway Pattern: Allows the entity layer to remain independent of
 * infrastructure details.
 */
public interface TenantGateway {

    /**
     * Create a new tenant record
     * 
     * @param tenantModel the tenant to create
     * @return the created tenant with ID assigned
     */
    TenantModel create(TenantModel tenantModel);

    /**
     * Update an existing tenant
     * 
     * @param tenantModel the tenant with updated values
     * @return the updated tenant
     */
    TenantModel update(TenantModel tenantModel);

    /**
     * Delete a tenant by ID
     * 
     * @param id the tenant ID
     * @throws TenantNotFoundException if tenant not found
     */
    void delete(UUID id) throws TenantNotFoundException;

    /**
     * Find a tenant by ID
     * 
     * @param id the tenant ID
     * @return Optional containing the tenant, or empty if not found
     */
    Optional<TenantModel> findById(UUID id);

    /**
     * Find a tenant by code
     * 
     * @param code the tenant code (short identifier)
     * @return Optional containing the tenant, or empty if not found
     */
    Optional<TenantModel> findByCode(String code);

    /**
     * Find a tenant by name
     * 
     * @param name the tenant name
     * @return Optional containing the tenant, or empty if not found
     */
    Optional<TenantModel> findByName(String name);

    /**
     * Find all active tenants
     * 
     * @return list of all active tenants
     */
    List<TenantModel> findAllActive();

    /**
     * Find all tenants regardless of status
     * 
     * @return list of all tenants
     */
    List<TenantModel> findAll();

    /**
     * Check if a tenant exists by ID
     * 
     * @param id the tenant ID
     * @return true if tenant exists, false otherwise
     */
    boolean existsById(UUID id);
}
