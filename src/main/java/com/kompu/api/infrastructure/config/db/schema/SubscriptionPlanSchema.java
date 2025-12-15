package com.kompu.api.infrastructure.config.db.schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.subscription.model.SubscriptionPlanModel;

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
 * SubscriptionPlanSchema - JPA entity mapping to subscription_plans table
 * Stores subscription plan definitions (BASIC, PRO, ENTERPRISE)
 */
@Entity
@Table(name = "subscription_plans", schema = "app", indexes = {
        @Index(name = "idx_subscription_plans_active", columnList = "is_active"),
        @Index(name = "idx_subscription_plans_name", columnList = "name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SubscriptionPlanSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; // 'BASIC', 'PRO', 'ENTERPRISE'

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(length = 10)
    @Builder.Default
    private String currency = "IDR";

    @Column(length = 20)
    @Builder.Default
    private String billingPeriod = "monthly"; // 'monthly', 'yearly'

    @Column
    @Builder.Default
    private Integer maxUsers = -1; // -1 = unlimited

    @Column
    @Builder.Default
    private Integer maxUnitUsaha = 2;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Constructor from domain model - converts SubscriptionPlanModel to schema
     */
    public SubscriptionPlanSchema(SubscriptionPlanModel model) {
        this.id = model.getId();
        this.name = model.getName();
        this.price = model.getPrice();
        this.currency = model.getCurrency();
        this.billingPeriod = model.getBillingPeriod();
        this.maxUsers = model.getMaxUsers();
        this.maxUnitUsaha = model.getMaxUnitUsaha();
        this.description = model.getDescription();
        this.isActive = model.getIsActive();
    }

    /**
     * Converts schema back to domain model
     */
    public SubscriptionPlanModel toSubscriptionPlanModel() {
        return SubscriptionPlanModel.builder()
                .id(this.id)
                .name(this.name)
                .price(this.price)
                .currency(this.currency)
                .billingPeriod(this.billingPeriod)
                .maxUsers(this.maxUsers)
                .maxUnitUsaha(this.maxUnitUsaha)
                .description(this.description)
                .isActive(this.isActive)
                .build();
    }
}
