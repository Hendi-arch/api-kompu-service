package com.kompu.api.infrastructure.config.db.repository;

import com.kompu.api.infrastructure.config.db.schema.DocumentSchema;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * DocumentRepository - JPA repository for document queries
 */
@Repository
public interface DocumentRepository extends JpaRepository<DocumentSchema, UUID> {

    Optional<DocumentSchema> findByTenantIdAndDocumentTypeAndDocumentNumber(
            UUID tenantId, String documentType, String documentNumber);

    List<DocumentSchema> findByOrderId(UUID orderId);

    List<DocumentSchema> findBySupplierId(UUID supplierId);

    @Query("SELECT d FROM DocumentSchema d WHERE d.tenantId = :tenantId AND d.deletedAt IS NULL ORDER BY d.createdAt DESC")
    List<DocumentSchema> findAllByTenant(UUID tenantId);

    @Query("SELECT d FROM DocumentSchema d WHERE d.tenantId = :tenantId AND d.documentType = :type AND d.deletedAt IS NULL")
    List<DocumentSchema> findByTenantAndType(UUID tenantId, String type);

    @Query("SELECT d FROM DocumentSchema d WHERE d.tenantId = :tenantId AND d.status = :status AND d.deletedAt IS NULL")
    List<DocumentSchema> findByTenantAndStatus(UUID tenantId, String status);

    @Query("SELECT d FROM DocumentSchema d WHERE d.tenantId = :tenantId AND d.generatedAt >= :startDate AND d.generatedAt <= :endDate AND d.deletedAt IS NULL")
    List<DocumentSchema> findByDateRange(UUID tenantId, Instant startDate, Instant endDate);
}
