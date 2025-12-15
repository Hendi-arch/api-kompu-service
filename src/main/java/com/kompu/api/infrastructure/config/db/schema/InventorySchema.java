package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.product.model.InventoryModel;

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
@Table(name = "inventories", schema = "app")
public class InventorySchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    private String location;

    @Column(nullable = false)
    @Builder.Default
    private Long quantity = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long reserved = 0L;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public InventorySchema(InventoryModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.productId = model.getProductId();
        this.location = model.getLocation();
        this.quantity = model.getQuantity();
        this.reserved = model.getReserved();
        this.updatedAt = model.getUpdatedAt();
        this.deletedAt = model.getDeletedAt();
    }

    public InventoryModel toModel() {
        return InventoryModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .productId(this.productId)
                .location(this.location)
                .quantity(this.quantity)
                .reserved(this.reserved)
                .updatedAt(this.updatedAt)
                .deletedAt(this.deletedAt)
                .build();
    }
}
