package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.tenantdomain.model.TenantDomainModel;

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
@Table(name = "tenant_domains", schema = "app")
public class TenantDomainSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String host;

    @Column(name = "is_primary")
    @Builder.Default
    private boolean isPrimary = false;

    @Column(name = "is_custom")
    @Builder.Default
    private boolean isCustom = false;

    @Column(name = "https_enabled")
    @Builder.Default
    private boolean httpsEnabled = true;

    @Column(name = "tls_provider")
    @Builder.Default
    private String tlsProvider = "cloudflare";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public TenantDomainSchema(TenantDomainModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.host = model.getHost();
        this.isPrimary = model.isPrimary();
        this.isCustom = model.isCustom();
        this.httpsEnabled = model.isHttpsEnabled();
        this.tlsProvider = model.getTlsProvider();
        this.createdAt = model.getCreatedAt();
        this.updatedAt = model.getUpdatedAt();
        this.deletedAt = model.getDeletedAt();
    }

    public TenantDomainModel toModel() {
        return TenantDomainModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .host(this.host)
                .primary(this.isPrimary)
                .custom(this.isCustom)
                .httpsEnabled(this.httpsEnabled)
                .tlsProvider(this.tlsProvider)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .deletedAt(this.deletedAt)
                .build();
    }
}
