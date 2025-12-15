package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.featureflag.model.FeatureFlagModel;

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
@Table(name = "feature_flags", schema = "app", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "tenant_id", "key" })
})
public class FeatureFlagSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String value;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public FeatureFlagSchema(FeatureFlagModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.key = model.getKey();
        this.value = model.getValue();
        this.enabled = model.isEnabled();
        this.createdAt = model.getCreatedAt();
        this.updatedAt = model.getUpdatedAt();
    }

    public FeatureFlagModel toModel() {
        return FeatureFlagModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .key(this.key)
                .value(this.value)
                .enabled(this.enabled)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
