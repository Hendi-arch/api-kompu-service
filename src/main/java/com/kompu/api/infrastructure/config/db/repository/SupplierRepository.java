package com.kompu.api.infrastructure.config.db.repository;

import com.kompu.api.infrastructure.config.db.schema.SupplierSchema;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * SupplierRepository - JPA repository for supplier queries
 */
@Repository
public interface SupplierRepository extends JpaRepository<SupplierSchema, UUID> {

    Optional<SupplierSchema> findByTenantIdAndSupplierCode(UUID tenantId, String supplierCode);

    Optional<SupplierSchema> findByEmail(String email);

    @Query("SELECT s FROM SupplierSchema s WHERE s.tenantId = :tenantId AND s.status = 'active' AND s.deletedAt IS NULL")
    List<SupplierSchema> findAllActiveByTenant(UUID tenantId);

    @Query("SELECT s FROM SupplierSchema s WHERE s.tenantId = :tenantId AND s.deletedAt IS NULL")
    List<SupplierSchema> findAllByTenant(UUID tenantId);

    @Query("SELECT s FROM SupplierSchema s WHERE s.tenantId = :tenantId AND s.supplierType = :type AND s.deletedAt IS NULL")
    List<SupplierSchema> findByTypeAndTenant(UUID tenantId, String type);

    @Query("SELECT s FROM SupplierSchema s WHERE s.tenantId = :tenantId AND LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND s.deletedAt IS NULL")
    List<SupplierSchema> searchByName(UUID tenantId, String searchTerm);
}
