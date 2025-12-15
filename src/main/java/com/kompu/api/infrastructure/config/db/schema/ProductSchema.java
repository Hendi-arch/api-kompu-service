package com.kompu.api.infrastructure.config.db.schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kompu.api.entity.product.model.ProductModel;

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
import lombok.extern.slf4j.Slf4j;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "products", schema = "app")
@Slf4j
public class ProductSchema {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    private String sku;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "cost_price", precision = 14, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

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

    public ProductSchema(ProductModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.sku = model.getSku();
        this.name = model.getName();
        this.description = model.getDescription();
        this.categoryId = model.getCategoryId();
        this.price = model.getPrice();
        this.costPrice = model.getCostPrice();
        this.weightGrams = model.getWeightGrams();
        this.isActive = model.getIsActive();
        if (model.getMetadata() != null) {
            try {
                this.metadata = mapper.writeValueAsString(model.getMetadata());
            } catch (JsonProcessingException e) {
                log.error("Error serializing metadata for product", e);
                this.metadata = "{}";
            }
        }
    }

    public ProductModel toModel() {
        JsonNode metadataNode = null;
        if (this.metadata != null) {
            try {
                metadataNode = mapper.readTree(this.metadata);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing metadata for product", e);
            }
        }

        return ProductModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .sku(this.sku)
                .name(this.name)
                .description(this.description)
                .categoryId(this.categoryId)
                .price(this.price)
                .costPrice(this.costPrice)
                .weightGrams(this.weightGrams)
                .isActive(this.isActive)
                .metadata(metadataNode)
                .build();
    }
}
