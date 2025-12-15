package com.kompu.api.infrastructure.config.db.schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.order.model.OrderModel;

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
@Table(name = "orders", schema = "app", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "tenant_id", "order_number" })
})
public class OrderSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "order_number", nullable = false)
    private String orderNumber;

    @Column(name = "order_type")
    @Builder.Default
    private String orderType = "sales";

    @Column(name = "buyer_id")
    private UUID buyerId;

    @Column(name = "supplier_id")
    private UUID supplierId;

    @Column(name = "buyer_snapshot", columnDefinition = "jsonb")
    private String buyerSnapshot;

    @Column(nullable = false)
    @Builder.Default
    private String status = "created";

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "IDR";

    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private String metadata = "{}";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public OrderSchema(OrderModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.orderNumber = model.getOrderNumber();
        this.orderType = model.getOrderType();
        this.buyerId = model.getBuyerId();
        this.supplierId = model.getSupplierId();
        this.buyerSnapshot = model.getBuyerSnapshot();
        this.status = model.getStatus();
        this.totalAmount = model.getTotalAmount();
        this.currency = model.getCurrency();
        this.metadata = model.getMetadata();
        this.createdAt = model.getCreatedAt();
        this.updatedAt = model.getUpdatedAt();
        this.createdBy = model.getCreatedBy();
        this.updatedBy = model.getUpdatedBy();
        this.deletedAt = model.getDeletedAt();
    }

    public OrderModel toModel() {
        return OrderModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .orderNumber(this.orderNumber)
                .orderType(this.orderType)
                .buyerId(this.buyerId)
                .supplierId(this.supplierId)
                .buyerSnapshot(this.buyerSnapshot)
                .status(this.status)
                .totalAmount(this.totalAmount)
                .currency(this.currency)
                .metadata(this.metadata)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .createdBy(this.createdBy)
                .updatedBy(this.updatedBy)
                .deletedAt(this.deletedAt)
                .build();
    }
}
