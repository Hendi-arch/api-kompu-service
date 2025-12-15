package com.kompu.api.entity.dashboardtheme.model;

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
public class DashboardThemeModel extends AbstractEntity<UUID> {

    private UUID id;
    private String name;
    private String displayName;
    private String description;
    private String previewImageUrl;
    private String themeConfig; // JSON string
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
