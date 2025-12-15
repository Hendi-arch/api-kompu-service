package com.kompu.api.entity.loan.model;

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
public class SavingsAccountModel extends AbstractEntity<UUID> {

    private UUID id;
    private UUID tenantId;
    private UUID memberId;
    private String accountNumber;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
