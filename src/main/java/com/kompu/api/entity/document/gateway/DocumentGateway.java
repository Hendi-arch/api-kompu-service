package com.kompu.api.entity.document.gateway;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.kompu.api.entity.document.model.DocumentModel;

/**
 * DocumentGateway - Gateway interface for document data access
 * Defines contract for managing generated documents
 */
public interface DocumentGateway {

    /**
     * Find document by ID
     */
    Optional<DocumentModel> findById(UUID id);

    /**
     * Find document by tenant and document number
     */
    Optional<DocumentModel> findByTenantAndNumber(UUID tenantId, String documentType, String documentNumber);

    /**
     * Find documents by order
     */
    List<DocumentModel> findByOrder(UUID orderId);

    /**
     * Find documents by supplier
     */
    List<DocumentModel> findBySupplier(UUID supplierId);

    /**
     * Find all documents for tenant
     */
    List<DocumentModel> findAllByTenant(UUID tenantId);

    /**
     * Find documents by type (PO, INVOICE, RECEIPT, etc.)
     */
    List<DocumentModel> findByType(UUID tenantId, String documentType);

    /**
     * Find documents by status
     */
    List<DocumentModel> findByStatus(UUID tenantId, String status);

    /**
     * Find documents created between dates
     */
    List<DocumentModel> findByDateRange(UUID tenantId, Instant startDate, Instant endDate);

    /**
     * Save new document
     */
    DocumentModel save(DocumentModel model);

    /**
     * Update document
     */
    DocumentModel update(DocumentModel model);

    /**
     * Delete document (soft delete)
     */
    void delete(UUID id);

    /**
     * Mark document as sent
     */
    void markAsSent(UUID id);

    /**
     * Mark document as verified (signature verified)
     */
    void markAsVerified(UUID id);

    /**
     * Archive document
     */
    void archive(UUID id);
}
