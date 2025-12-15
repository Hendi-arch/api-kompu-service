package com.kompu.api.entity.supplier.model;

import java.math.BigDecimal;
import java.time.LocalDate;
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
public class SupplierProductPriceModel extends AbstractEntity<UUID> {

    private UUID id;
    private UUID tenantId;
    private UUID supplierId;
    private UUID productId;
    private BigDecimal unitPrice;
    private String currency;
    private Integer minimumOrderQty;
    private Integer maximumOrderQty;
    private Integer leadTimeDays;
    private boolean isActive;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
