package com.kompu.api.entity.supplier.model;

import java.time.Instant;
import java.util.UUID;

import com.kompu.api.entity.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * SupplierModel - Domain model for supplier/vendor management
 * Represents suppliers, wholesalers, distributors in the supply chain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SupplierModel extends AbstractEntity<UUID> {

    private UUID id;
    private UUID tenantId;
    private String supplierCode;
    private String supplierName;
    private String supplierType; // 'producer', 'distributor', 'wholesaler'
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String province;
    private String postalCode;
    private String bankAccount;
    private String bankName;
    private String taxId; // NPWP/VAT ID
    @Builder.Default
    private String status = "active"; // 'active', 'inactive', 'blacklisted'
    private Double rating; // 1-5 stars
    private String paymentTerms; // 'Net 30', 'COD', etc.
    private String notes;
    @Builder.Default
    private String metadata = "{}";
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

}
