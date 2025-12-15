package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.system.model.LoginLogModel;

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
@Table(name = "login_log", schema = "app")
public class LoginLogSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "user_id")
    private UUID userId;

    private String email;

    private String ip;

    @Column(name = "user_agent")
    private String userAgent;

    private String result;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public LoginLogSchema(LoginLogModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.userId = model.getUserId();
        this.email = model.getEmail();
        this.ip = model.getIp();
        this.userAgent = model.getUserAgent();
        this.result = model.getResult();
        this.createdAt = model.getCreatedAt();
    }

    public LoginLogModel toModel() {
        return LoginLogModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .userId(this.userId)
                .email(this.email)
                .ip(this.ip)
                .userAgent(this.userAgent)
                .result(this.result)
                .createdAt(this.createdAt)
                .build();
    }
}
