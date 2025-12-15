package com.kompu.api.infrastructure.document.gateway;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.kompu.api.entity.document.gateway.DocumentGateway;
import com.kompu.api.entity.document.model.DocumentModel;
import com.kompu.api.infrastructure.config.db.repository.DocumentRepository;
import com.kompu.api.infrastructure.config.db.schema.DocumentSchema;

import lombok.RequiredArgsConstructor;

/**
 * DocumentDatabaseGateway - Database implementation of DocumentGateway
 * Converts between domain models and database schemas
 */
@Component
@RequiredArgsConstructor
public class DocumentDatabaseGateway implements DocumentGateway {

    private final DocumentRepository documentRepository;

    @Override
    public Optional<DocumentModel> findById(UUID id) {
        return documentRepository.findById(id).map(DocumentSchema::toDocumentModel);
    }

    @Override
    public Optional<DocumentModel> findByTenantAndNumber(UUID tenantId, String documentType, String documentNumber) {
        return documentRepository
                .findByTenantIdAndDocumentTypeAndDocumentNumber(tenantId, documentType, documentNumber)
                .map(DocumentSchema::toDocumentModel);
    }

    @Override
    public List<DocumentModel> findByOrder(UUID orderId) {
        return documentRepository.findByOrderId(orderId).stream().map(DocumentSchema::toDocumentModel).toList();
    }

    @Override
    public List<DocumentModel> findBySupplier(UUID supplierId) {
        return documentRepository.findBySupplierId(supplierId).stream().map(DocumentSchema::toDocumentModel).toList();
    }

    @Override
    public List<DocumentModel> findAllByTenant(UUID tenantId) {
        return documentRepository.findAllByTenant(tenantId).stream().map(DocumentSchema::toDocumentModel).toList();
    }

    @Override
    public List<DocumentModel> findByType(UUID tenantId, String documentType) {
        return documentRepository.findByTenantAndType(tenantId, documentType).stream()
                .map(DocumentSchema::toDocumentModel).toList();
    }

    @Override
    public List<DocumentModel> findByStatus(UUID tenantId, String status) {
        return documentRepository.findByTenantAndStatus(tenantId, status).stream().map(DocumentSchema::toDocumentModel)
                .toList();
    }

    @Override
    public List<DocumentModel> findByDateRange(UUID tenantId, Instant startDate, Instant endDate) {
        return documentRepository.findByDateRange(tenantId, startDate, endDate).stream()
                .map(DocumentSchema::toDocumentModel).toList();
    }

    @Override
    public DocumentModel save(DocumentModel model) {
        return documentRepository.save(new DocumentSchema(model)).toDocumentModel();
    }

    @Override
    public DocumentModel update(DocumentModel model) {
        DocumentSchema schema = documentRepository
                .findById(model.getId())
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        schema.setStatus(model.getStatus());
        schema.setSentAt(model.getSentAt());
        schema.setSignatureHash(model.getSignatureHash());

        DocumentSchema updated = documentRepository.save(schema);
        return updated.toDocumentModel();
    }

    @Override
    public void delete(UUID id) {
        documentRepository
                .findById(id)
                .ifPresent(
                        doc -> {
                            doc.setDeletedAt(Instant.now());
                            documentRepository.save(doc);
                        });
    }

    @Override
    public void markAsSent(UUID id) {
        documentRepository
                .findById(id)
                .ifPresent(
                        doc -> {
                            doc.setStatus("sent");
                            doc.setSentAt(Instant.now());
                            documentRepository.save(doc);
                        });
    }

    @Override
    public void markAsVerified(UUID id) {
        documentRepository
                .findById(id)
                .ifPresent(
                        doc -> {
                            doc.setStatus("verified");
                            documentRepository.save(doc);
                        });
    }

    @Override
    public void archive(UUID id) {
        documentRepository
                .findById(id)
                .ifPresent(
                        doc -> {
                            doc.setStatus("archived");
                            documentRepository.save(doc);
                        });
    }

}
