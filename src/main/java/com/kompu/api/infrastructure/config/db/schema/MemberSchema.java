package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.member.model.MemberModel;

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
@Table(name = "members", schema = "app")
public class MemberSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "member_code")
    private String memberCode;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String email;

    private String phone;

    private String address;

    @Column(name = "joined_at")
    private LocalDate joinedAt;

    @Builder.Default
    private String status = "active";

    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private String metadata = "{}";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public MemberSchema(MemberModel model) {
        this.id = model.getId();
        this.tenantId = model.getTenantId();
        this.memberCode = model.getMemberCode();
        this.userId = model.getUserId();
        this.fullName = model.getFullName();
        this.email = model.getEmail();
        this.phone = model.getPhone();
        this.address = model.getAddress();
        this.joinedAt = model.getJoinedAt();
        this.status = model.getStatus();
        this.metadata = model.getMetadata();
        this.createdAt = model.getCreatedAt();
        this.updatedAt = model.getUpdatedAt();
        this.deletedAt = model.getDeletedAt();
        if (model.getCreatedBy() != null) {
            try {
                this.createdBy = UUID.fromString(model.getCreatedBy());
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        if (model.getUpdatedBy() != null) {
            try {
                this.updatedBy = UUID.fromString(model.getUpdatedBy());
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
    }

    public MemberModel toModel() {
        return MemberModel.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .memberCode(this.memberCode)
                .userId(this.userId)
                .fullName(this.fullName)
                .email(this.email)
                .phone(this.phone)
                .address(this.address)
                .joinedAt(this.joinedAt)
                .status(this.status)
                .metadata(this.metadata)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .createdBy(this.createdBy != null ? this.createdBy.toString() : null)
                .updatedBy(this.updatedBy != null ? this.updatedBy.toString() : null)
                .deletedAt(this.deletedAt)
                .build();
    }
}
