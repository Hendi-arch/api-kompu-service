package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.usertoken.model.RevokedJtiModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "revoked_jtis", schema = "app")
public class RevokedJtiSchema {

    @Id
    private UUID jti;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_revoked_jtis_user_id"))
    private UserSchema user;

    @CreatedDate
    @Column(name = "revoked_at", nullable = false)
    private LocalDateTime revokedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public RevokedJtiSchema(RevokedJtiModel revokedJtiModel) {
        this.jti = revokedJtiModel.getJti();
        this.userId = revokedJtiModel.getUserId();
        this.revokedAt = revokedJtiModel.getRevokedAt();
        this.expiresAt = revokedJtiModel.getExpiresAt();
    }

    public RevokedJtiModel toRevokedJtiModel() {
        return RevokedJtiModel.builder()
                .jti(this.jti)
                .userId(this.userId)
                .revokedAt(this.revokedAt)
                .expiresAt(this.expiresAt)
                .build();
    }

}
