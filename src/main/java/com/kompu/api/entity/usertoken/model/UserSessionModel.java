package com.kompu.api.entity.usertoken.model;

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
public class UserSessionModel extends AbstractEntity<UUID> {

    private UUID id;

    private UUID tenantId;

    private UUID userId;

    private String ipAddress;

    private String userAgent;

    private LocalDateTime createdAt;

    private LocalDateTime lastActiveAt;

    private boolean isActive;

    private LocalDateTime deletedAt;

}
