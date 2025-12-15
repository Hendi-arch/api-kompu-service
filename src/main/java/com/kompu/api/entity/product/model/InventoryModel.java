package com.kompu.api.entity.product.model;

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
public class InventoryModel extends AbstractEntity<UUID> {

    private UUID id;
    private UUID tenantId;
    private UUID productId;
    private String location; // warehouse or branch id/name
    private Long quantity;
    private Long reserved;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
