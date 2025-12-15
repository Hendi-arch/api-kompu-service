package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.tenant.model.TenantModel;

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
@Table(name = "tenants", schema = "app")
public class TenantSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String code;

    @Column(nullable = false)
    @Builder.Default
    private String status = "active";

    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private String metadata = "{}";

    @Column(name = "theme_id")
    private UUID themeId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public TenantSchema(TenantModel model) {
        this.id = model.getId();
        this.name = model.getName();
        this.code = model.getCode();
        this.status = model.getStatus();
        this.metadata = model.getMetadata();
        this.themeId = model.getThemeId();
        this.createdAt = model.getCreatedAt();
        this.updatedAt = model.getUpdatedAt();
        this.deletedAt = model.getDeletedAt();
        if (model.getCreatedBy() != null) {
            try {
                this.createdBy = UUID.fromString(model.getCreatedBy());
            } catch (IllegalArgumentException e) {
                // Handle or ignore invalid UUID string from model if necessary
            }
        }
        if (model.getUpdatedBy() != null) {
            try {
                this.updatedBy = UUID.fromString(model.getUpdatedBy());
            } catch (IllegalArgumentException e) {
                // Handle or ignore
            }
        }
    }

    public TenantModel toModel() {
        return TenantModel.builder()
                .id(this.id)
                .name(this.name)
                .code(this.code)
                .status(this.status)
                .metadata(this.metadata)
                .themeId(this.themeId)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .createdBy(this.createdBy != null ? this.createdBy.toString() : null)
                .updatedBy(this.updatedBy != null ? this.updatedBy.toString() : null)
                .deletedAt(this.deletedAt)
                .build();
    }
}
