package com.kompu.api.infrastructure.config.db.schema;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.supplier.model.SupplierModel;

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
 * SupplierSchema - JPA entity mapping to suppliers table
 * Stores supplier/vendor information for procurement
 */
@Entity
@Table(name = "suppliers", schema = "app", indexes = {
        @Index(name = "idx_suppliers_tenant_code", columnList = "tenant_id,supplier_code"),
        @Index(name = "idx_suppliers_status", columnList = "status"),
        @Index(name = "idx_suppliers_tenant", columnList = "tenant_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SupplierSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(nullable = false, length = 50)
    private String supplierCode;

    @Column(nullable = false, length = 255)
    private String supplierName;

    @Column(length = 50)
    private String supplierType; // 'producer', 'distributor', 'wholesaler'

    @Column(length = 255)
    private String contactPerson;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "text")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String province;

    @Column(length = 10)
    private String postalCode;

    @Column(length = 50)
    private String bankAccount;

    @Column(length = 100)
    private String bankName;

    @Column(length = 50)
    private String taxId;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "active"; // 'active', 'inactive', 'blacklisted'

    @Column(precision = 2, scale = 1)
    private Double rating; // 1-5 stars

    @Column(length = 100)
    private String paymentTerms;

    @Column(columnDefinition = "text")
    private String notes;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(columnDefinition = "uuid")
    private UUID createdBy;

    @Column(columnDefinition = "uuid")
    private UUID updatedBy;

    @Column
    private Instant deletedAt;

    /**
     * Constructor from domain model - converts SupplierModel to schema
     */
    public SupplierSchema(SupplierModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.supplierCode = model.getSupplierCode();
        this.supplierName = model.getSupplierName();
        this.supplierType = model.getSupplierType();
        this.contactPerson = model.getContactPerson();
        this.email = model.getEmail();
        this.phone = model.getPhone();
        this.address = model.getAddress();
        this.city = model.getCity();
        this.province = model.getProvince();
        this.postalCode = model.getPostalCode();
        this.bankAccount = model.getBankAccount();
        this.bankName = model.getBankName();
        this.taxId = model.getTaxId();
        this.status = model.getStatus();
        this.rating = model.getRating();
        this.paymentTerms = model.getPaymentTerms();
        this.notes = model.getNotes();
        this.createdBy = model.getCreatedBy();
        this.updatedBy = model.getUpdatedBy();
    }

    /**
     * Converts schema back to domain model
     */
    public SupplierModel toSupplierModel() {
        return SupplierModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .supplierCode(this.supplierCode)
                .supplierName(this.supplierName)
                .supplierType(this.supplierType)
                .contactPerson(this.contactPerson)
                .email(this.email)
                .phone(this.phone)
                .address(this.address)
                .city(this.city)
                .province(this.province)
                .postalCode(this.postalCode)
                .bankAccount(this.bankAccount)
                .bankName(this.bankName)
                .taxId(this.taxId)
                .status(this.status)
                .rating(this.rating)
                .paymentTerms(this.paymentTerms)
                .notes(this.notes)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .createdBy(this.createdBy)
                .updatedBy(this.updatedBy)
                .build();
    }
}
