package com.kompu.api.infrastructure.config.db.schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.order.model.OrderItemModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "order_items", schema = "app")
public class OrderItemSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_snapshot", columnDefinition = "jsonb")
    private String productSnapshot;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public OrderItemSchema(OrderItemModel model) {
        this.id = model.getId();
        this.orderId = model.getOrderId();
        this.productId = model.getProductId();
        this.productSnapshot = model.getProductSnapshot();
        this.quantity = model.getQuantity();
        this.unitPrice = model.getUnitPrice();
        this.createdAt = model.getCreatedAt();
        this.deletedAt = model.getDeletedAt();
    }

    public OrderItemModel toModel() {
        return OrderItemModel.builder()
                .id(this.id)
                .orderId(this.orderId)
                .productId(this.productId)
                .productSnapshot(this.productSnapshot)
                .quantity(this.quantity)
                .unitPrice(this.unitPrice)
                .createdAt(this.createdAt)
                .deletedAt(this.deletedAt)
                .build();
    }
}
