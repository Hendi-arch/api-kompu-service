package com.kompu.api.infrastructure.config.db.schema;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.document.model.DocumentModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DocumentSchema - JPA entity mapping to documents table
 * Tracks generated documents (PO, Invoice, Receipt, etc.)
 */
@Entity
@Table(name = "documents", schema = "app", indexes = {
        @Index(name = "idx_documents_tenant_number", columnList = "tenant_id,document_type,document_number"),
        @Index(name = "idx_documents_order", columnList = "order_id"),
        @Index(name = "idx_documents_supplier", columnList = "supplier_id"),
        @Index(name = "idx_documents_status", columnList = "status"),
        @Index(name = "idx_documents_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DocumentSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(nullable = false, length = 50)
    private String documentType; // 'PO', 'INVOICE', 'RECEIPT', etc.

    @Column(nullable = false, length = 100)
    private String documentNumber;

    @Column(columnDefinition = "uuid")
    private UUID orderId;

    @Column(columnDefinition = "uuid")
    private UUID supplierId;

    @Column(columnDefinition = "text")
    private String contentPath; // S3 or file server path

    @Column(length = 20)
    @Builder.Default
    private String contentFormat = "pdf"; // 'pdf', 'xml', 'json'

    @Column(nullable = false)
    @Builder.Default
    private Instant generatedAt = Instant.now();

    @Column
    private Instant sentAt;

    @Column(length = 256)
    private String signatureHash;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "generated"; // 'generated', 'sent', 'verified', 'archived'

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(columnDefinition = "uuid")
    private UUID createdBy;

    @Column
    private Instant deletedAt;

    /**
     * Constructor from domain model - converts DocumentModel to schema
     */
    public DocumentSchema(DocumentModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.documentType = model.getDocumentType();
        this.documentNumber = model.getDocumentNumber();
        this.orderId = model.getOrderId();
        this.supplierId = model.getSupplierId();
        this.contentPath = model.getContentPath();
        this.contentFormat = model.getContentFormat();
        this.generatedAt = model.getGeneratedAt();
        this.sentAt = model.getSentAt();
        this.signatureHash = model.getSignatureHash();
        this.status = model.getStatus();
        this.createdBy = model.getCreatedBy();
    }

    /**
     * Converts schema back to domain model
     */
    public DocumentModel toDocumentModel() {
        return DocumentModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .documentType(this.documentType)
                .documentNumber(this.documentNumber)
                .orderId(this.orderId)
                .supplierId(this.supplierId)
                .contentPath(this.contentPath)
                .contentFormat(this.contentFormat)
                .generatedAt(this.generatedAt)
                .sentAt(this.sentAt)
                .signatureHash(this.signatureHash)
                .status(this.status)
                .createdAt(this.createdAt)
                .createdBy(this.createdBy)
                .build();
    }
}
