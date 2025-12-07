package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.usertoken.model.RefreshTokenModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "refresh_tokens", schema = "app", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
        "token_hash" }))
public class RefreshTokenSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_refresh_tokens_user_id"))
    private UserSchema user;

    @Column(name = "session_id")
    private UUID sessionId;

    @ManyToOne
    @JoinColumn(name = "session_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_refresh_tokens_session_id"))
    private UserSessionSchema session;

    @Column(name = "token_hash", nullable = false, columnDefinition = "bytea")
    private String tokenHash;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public RefreshTokenSchema(RefreshTokenModel refreshTokenModel) {
        this.id = refreshTokenModel.getId();
        this.userId = refreshTokenModel.getUserId();
        this.sessionId = refreshTokenModel.getSessionId();
        this.tokenHash = refreshTokenModel.getTokenHash();
        this.createdAt = refreshTokenModel.getCreatedAt();
        this.expiresAt = refreshTokenModel.getExpiresAt();
        this.revokedAt = refreshTokenModel.getRevokedAt();
    }

    public RefreshTokenModel toRefreshTokenModel() {
        return RefreshTokenModel.builder()
                .id(this.id)
                .userId(this.userId)
                .sessionId(this.sessionId)
                .tokenHash(this.tokenHash)
                .createdAt(this.createdAt)
                .expiresAt(this.expiresAt)
                .revokedAt(this.revokedAt)
                .build();
    }

}
