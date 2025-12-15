package com.kompu.api.infrastructure.config.db.schema;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.subscriptionfeature.model.SubscriptionFeatureModel;

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
@Table(name = "subscription_features", schema = "app")
public class SubscriptionFeatureSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "feature_key", nullable = false, unique = true)
    private String featureKey;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 50)
    private String category;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public SubscriptionFeatureSchema(SubscriptionFeatureModel model) {
        this.id = model.getId();
        this.featureKey = model.getFeatureKey();
        this.displayName = model.getDisplayName();
        this.description = model.getDescription();
        this.category = model.getCategory();
    }

    public SubscriptionFeatureModel toModel() {
        return SubscriptionFeatureModel.builder()
                .id(this.id)
                .featureKey(this.featureKey)
                .displayName(this.displayName)
                .description(this.description)
                .category(this.category)
                .build();
    }
}
