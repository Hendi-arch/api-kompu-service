package com.kompu.api.infrastructure.config.db.schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.subscriptioninvoice.model.SubscriptionInvoiceModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "subscription_invoices", schema = "app", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "tenant_id", "invoice_number" })
})
public class SubscriptionInvoiceSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;

    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "IDR";

    @Column(name = "billing_period_start", nullable = false)
    private LocalDate billingPeriodStart;

    @Column(name = "billing_period_end", nullable = false)
    private LocalDate billingPeriodEnd;

    @Column(nullable = false)
    @Builder.Default
    private String status = "draft";

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public SubscriptionInvoiceSchema(SubscriptionInvoiceModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.subscriptionId = model.getSubscriptionId();
        this.invoiceNumber = model.getInvoiceNumber();
        this.amount = model.getAmount();
        this.currency = model.getCurrency();
        this.billingPeriodStart = model.getBillingPeriodStart();
        this.billingPeriodEnd = model.getBillingPeriodEnd();
        this.status = model.getStatus();
        this.issuedAt = model.getIssuedAt();
        this.dueAt = model.getDueAt();
        this.paidAt = model.getPaidAt();
        this.createdAt = model.getCreatedAt();
        this.updatedAt = model.getUpdatedAt();
    }

    public SubscriptionInvoiceModel toModel() {
        return SubscriptionInvoiceModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .subscriptionId(this.subscriptionId)
                .invoiceNumber(this.invoiceNumber)
                .amount(this.amount)
                .currency(this.currency)
                .billingPeriodStart(this.billingPeriodStart)
                .billingPeriodEnd(this.billingPeriodEnd)
                .status(this.status)
                .issuedAt(this.issuedAt)
                .dueAt(this.dueAt)
                .paidAt(this.paidAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
