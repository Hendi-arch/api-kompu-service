package com.kompu.api.infrastructure.config.db.schema;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.subscription.model.TenantSubscriptionModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TenantSubscriptionSchema - JPA entity mapping to tenant_subscriptions table
 * Tracks active subscription for each tenant
 */
@Entity
@Table(name = "tenant_subscriptions", schema = "app", indexes = {
        @Index(name = "idx_tenant_subscriptions_tenant", columnList = "tenant_id"),
        @Index(name = "idx_tenant_subscriptions_status", columnList = "status"),
        @Index(name = "idx_tenant_subscriptions_plan", columnList = "plan_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TenantSubscriptionSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid", unique = true)
    private UUID tenantId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID planId;

    @Column(nullable = false)
    private String subscriptionStartDate; // ISO date

    @Column
    private String subscriptionEndDate; // ISO date (nullable)

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "active"; // 'trial', 'active', 'suspended', 'cancelled', 'expired'

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoRenew = true;

    @Column
    private Instant trialEndsAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(columnDefinition = "uuid")
    private UUID createdBy;

    @Column(columnDefinition = "uuid")
    private UUID updatedBy;

    /**
     * Constructor from domain model - converts TenantSubscriptionModel to schema
     */
    public TenantSubscriptionSchema(TenantSubscriptionModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.planId = model.getPlanId();
        this.subscriptionStartDate = model.getSubscriptionStartDate();
        this.subscriptionEndDate = model.getSubscriptionEndDate();
        this.status = model.getStatus();
        this.autoRenew = model.getAutoRenew();
        this.trialEndsAt = model.getTrialEndsAt();
        this.createdBy = model.getCreatedBy();
        this.updatedBy = model.getUpdatedBy();
    }

    /**
     * Converts schema back to domain model
     */
    public TenantSubscriptionModel toTenantSubscriptionModel() {
        return TenantSubscriptionModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .planId(this.planId)
                .subscriptionStartDate(this.subscriptionStartDate)
                .subscriptionEndDate(this.subscriptionEndDate)
                .status(this.status)
                .autoRenew(this.autoRenew)
                .trialEndsAt(this.trialEndsAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .createdBy(this.createdBy)
                .updatedBy(this.updatedBy)
                .build();
    }
}
