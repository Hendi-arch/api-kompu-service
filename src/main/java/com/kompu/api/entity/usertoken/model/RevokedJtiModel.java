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
public class RevokedJtiModel extends AbstractEntity<UUID> {

    private UUID jti;

    private UUID userId;

    private LocalDateTime revokedAt;

    private LocalDateTime expiresAt;

}
