package com.kompu.api.entity.order.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.kompu.api.entity.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OrderModel extends AbstractEntity<UUID> {

    private UUID id;
    private UUID tenantId;
    private String orderNumber;
    private String orderType; // 'sales' or 'purchase'
    private UUID buyerId; // for sales orders
    private UUID supplierId; // for purchase orders
    private String buyerSnapshot; // JSON string
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private String metadata; // JSON string
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime deletedAt;
}
