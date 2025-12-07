package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.usertoken.model.UserSessionModel;

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
@Table(name = "user_sessions", schema = "app")
public class UserSessionSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_user_sessions_user_id"))
    private UserSchema user;

    @Column(name = "ip", columnDefinition = "inet")
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public UserSessionSchema(UserSessionModel userSessionModel) {
        this.id = userSessionModel.getId();
        this.tenantId = userSessionModel.getTenantId();
        this.userId = userSessionModel.getUserId();
        this.ipAddress = userSessionModel.getIpAddress();
        this.userAgent = userSessionModel.getUserAgent();
        this.createdAt = userSessionModel.getCreatedAt();
        this.lastActiveAt = userSessionModel.getLastActiveAt();
        this.isActive = userSessionModel.isActive();
        this.deletedAt = userSessionModel.getDeletedAt();
    }

    public UserSessionModel toUserSessionModel() {
        return UserSessionModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .userId(this.userId)
                .ipAddress(this.ipAddress)
                .userAgent(this.userAgent)
                .createdAt(this.createdAt)
                .lastActiveAt(this.lastActiveAt)
                .isActive(this.isActive)
                .deletedAt(this.deletedAt)
                .build();
    }

}
