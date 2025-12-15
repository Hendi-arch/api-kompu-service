package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.dashboardtheme.model.DashboardThemeModel;

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
@Table(name = "dashboard_themes", schema = "app")
public class DashboardThemeSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(length = 1000)
    private String description;

    @Column(name = "preview_image_url")
    private String previewImageUrl;

    @Column(name = "theme_config", columnDefinition = "jsonb")
    private String themeConfig;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public DashboardThemeSchema(DashboardThemeModel model) {
        this.id = model.getId();
        this.name = model.getName();
        this.displayName = model.getDisplayName();
        this.description = model.getDescription();
        this.previewImageUrl = model.getPreviewImageUrl();
        this.themeConfig = model.getThemeConfig();
        this.isActive = model.isActive();
        this.createdAt = model.getCreatedAt();
        this.updatedAt = model.getUpdatedAt();
    }

    public DashboardThemeModel toModel() {
        return DashboardThemeModel.builder()
                .id(this.id)
                .name(this.name)
                .displayName(this.displayName)
                .description(this.description)
                .previewImageUrl(this.previewImageUrl)
                .themeConfig(this.themeConfig)
                .isActive(this.isActive)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
