package com.kompu.api.entity.loan.model;

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
 * LoanModel - Domain model for member loan management
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoanModel extends AbstractEntity<UUID> {

    private UUID id;
    private UUID tenantId;
    private UUID memberId;
    private String loanNumber;
    private BigDecimal principal;
    private BigDecimal outstanding;
    private String status;
    private JsonNode terms;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
