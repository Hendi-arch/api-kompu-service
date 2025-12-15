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
public class LoginLogModel extends AbstractEntity<Long> {

    private Long id;
    private UUID tenantId;
    private UUID userId;
    private String email;
    private String ip;
    private String userAgent;
    private String result; // 'success', 'failed', 'blocked'
    private LocalDateTime createdAt;
}
