package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.system.model.ActivityLogModel;

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
@Table(name = "activity_log", schema = "app")
public class ActivityLogSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "actor_snapshot", columnDefinition = "jsonb")
    private String actorSnapshot;

    @Column(nullable = false)
    private String action;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ActivityLogSchema(ActivityLogModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.userId = model.getUserId();
        this.actorSnapshot = model.getActorSnapshot();
        this.action = model.getAction();
        this.resourceType = model.getResourceType();
        this.resourceId = model.getResourceId();
        this.payload = model.getPayload();
        this.createdAt = model.getCreatedAt();
    }

    public ActivityLogModel toModel() {
        return ActivityLogModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .userId(this.userId)
                .actorSnapshot(this.actorSnapshot)
                .action(this.action)
                .resourceType(this.resourceType)
                .resourceId(this.resourceId)
                .payload(this.payload)
                .createdAt(this.createdAt)
                .build();
    }
}
