package com.kompu.api.entity.subscriptioninvoice.model;

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
public class SubscriptionInvoiceModel extends AbstractEntity<UUID> {

    private UUID id;
    private UUID tenantId;
    private UUID subscriptionId;
    private String invoiceNumber;
    private BigDecimal amount;
    private String currency;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private String status; // 'draft', 'sent', 'paid', 'failed', 'refunded'
    private LocalDateTime issuedAt;
    private LocalDateTime dueAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
