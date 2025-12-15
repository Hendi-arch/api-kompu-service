package com.kompu.api.entity.document.model;

import java.time.Instant;
import java.util.UUID;

import com.kompu.api.entity.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DocumentModel - Domain model for document management
 * Tracks generated documents (PO, Invoice, Receipt, Shipping Letter, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DocumentModel extends AbstractEntity<UUID> {

    private UUID id;
    private UUID tenantId;
    private String documentType;
    private String documentNumber;
    private UUID orderId;
    private UUID supplierId;
    private String contentPath;
    @Builder.Default
    private String contentFormat = "pdf";
    private Instant generatedAt;
    private Instant sentAt;
    private String signatureHash;
    @Builder.Default
    private String status = "generated";
    private Instant createdAt;
    private UUID createdBy;

}
