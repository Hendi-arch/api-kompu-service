package com.kompu.api.entity.product.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.kompu.api.entity.AbstractEntity;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ProductModel - Domain model for product master data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductModel extends AbstractEntity<UUID> {

    private UUID id;
    private UUID tenantId;
    private String sku;
    private String name;
    private String description;
    private UUID categoryId;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer weightGrams;
    private Boolean isActive;
    private JsonNode metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
