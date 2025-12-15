package com.kompu.api.infrastructure.config.db.schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.payment.model.PaymentModel;

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
@Table(name = "payments", schema = "app")
public class PaymentSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "payment_provider")
    private String paymentProvider;

    @Column(name = "provider_reference")
    private String providerReference;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "IDR";

    @Column(nullable = false)
    @Builder.Default
    private String status = "pending";

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public PaymentSchema(PaymentModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.orderId = model.getOrderId();
        this.paymentProvider = model.getPaymentProvider();
        this.providerReference = model.getProviderReference();
        this.amount = model.getAmount();
        this.currency = model.getCurrency();
        this.status = model.getStatus();
        this.paidAt = model.getPaidAt();
        this.createdAt = model.getCreatedAt();
        this.deletedAt = model.getDeletedAt();
    }

    public PaymentModel toModel() {
        return PaymentModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .orderId(this.orderId)
                .paymentProvider(this.paymentProvider)
                .providerReference(this.providerReference)
                .amount(this.amount)
                .currency(this.currency)
                .status(this.status)
                .paidAt(this.paidAt)
                .createdAt(this.createdAt)
                .deletedAt(this.deletedAt)
                .build();
    }
}
