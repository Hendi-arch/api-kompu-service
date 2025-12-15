package com.kompu.api.entity.supplier.gateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.supplier.model.SupplierModel;

/**
 * SupplierGateway - Gateway interface for supplier data access
 * Defines contract for CRUD operations on suppliers
 */
public interface SupplierGateway {

    /**
     * Find supplier by ID
     */
    Optional<SupplierModel> findById(UUID id);

    /**
     * Find supplier by tenant and supplier code
     */
    Optional<SupplierModel> findByTenantAndCode(UUID tenantId, String supplierCode);

    /**
     * Find supplier by email
     */
    Optional<SupplierModel> findByEmail(String email);

    /**
     * Find all active suppliers for tenant
     */
    List<SupplierModel> findAllActiveByTenant(UUID tenantId);

    /**
     * Find all suppliers for tenant (including inactive)
     */
    List<SupplierModel> findAllByTenant(UUID tenantId);

    /**
     * Find suppliers by type (producer, distributor, wholesaler)
     */
    List<SupplierModel> findByType(UUID tenantId, String supplierType);

    /**
     * Save new supplier
     */
    SupplierModel save(SupplierModel model);

    /**
     * Update supplier
     */
    SupplierModel update(SupplierModel model);

    /**
     * Delete supplier (soft delete)
     */
    void delete(UUID id);

    /**
     * Search suppliers by name
     */
    List<SupplierModel> searchByName(UUID tenantId, String searchTerm);
}
