package com.kompu.api.infrastructure.config.db.schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.supplier.model.SupplierProductPriceModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "supplier_product_prices", schema = "app", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "supplier_id", "product_id", "valid_from" })
})
public class SupplierProductPriceSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "supplier_id", nullable = false)
    private UUID supplierId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "unit_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "IDR";

    @Column(name = "minimum_order_qty")
    @Builder.Default
    private Integer minimumOrderQty = 1;

    @Column(name = "maximum_order_qty")
    private Integer maximumOrderQty;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "valid_from", nullable = false)
    @Builder.Default
    private LocalDate validFrom = LocalDate.now();

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(columnDefinition = "text")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public SupplierProductPriceSchema(SupplierProductPriceModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.supplierId = model.getSupplierId();
        this.productId = model.getProductId();
        this.unitPrice = model.getUnitPrice();
        this.currency = model.getCurrency();
        this.minimumOrderQty = model.getMinimumOrderQty();
        this.maximumOrderQty = model.getMaximumOrderQty();
        this.leadTimeDays = model.getLeadTimeDays();
        this.isActive = model.isActive();
        this.validFrom = model.getValidFrom();
        this.validUntil = model.getValidUntil();
        this.notes = model.getNotes();
        this.createdAt = model.getCreatedAt();
        this.updatedAt = model.getUpdatedAt();
    }

    public SupplierProductPriceModel toModel() {
        return SupplierProductPriceModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .supplierId(this.supplierId)
                .productId(this.productId)
                .unitPrice(this.unitPrice)
                .currency(this.currency)
                .minimumOrderQty(this.minimumOrderQty)
                .maximumOrderQty(this.maximumOrderQty)
                .leadTimeDays(this.leadTimeDays)
                .isActive(this.isActive)
                .validFrom(this.validFrom)
                .validUntil(this.validUntil)
                .notes(this.notes)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
