package com.kompu.api.entity.payment.model;

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
public class PaymentModel extends AbstractEntity<UUID> {

    private UUID id;
    private UUID tenantId;
    private UUID orderId;
    private String paymentProvider;
    private String providerReference;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
