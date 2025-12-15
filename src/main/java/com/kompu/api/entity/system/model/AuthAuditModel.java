package com.kompu.api.entity.system.model;

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
public class AuthAuditModel extends AbstractEntity<Long> {

    private Long id;
    private UUID tenantId;
    private UUID userId;
    private UUID actorUserId;
    private String action; // 'refresh_issued','refresh_revoked','jti_revoked','logout'
    private String resourceType;
    private UUID resourceId;
    private String payload; // json string
    private LocalDateTime createdAt;
}
