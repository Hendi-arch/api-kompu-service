package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.user.model.UserAccountModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@Table(name = "users", schema = "app", uniqueConstraints = {
                @UniqueConstraint(columnNames = { "tenant_id", "email" }),
                @UniqueConstraint(columnNames = { "tenant_id", "username" })
})
public class UserSchema {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @Column(name = "tenant_id")
        private UUID tenantId;

        @Column(nullable = false, length = 100)
        private String username;

        @Column(nullable = false, length = 255)
        private String email;

        @Column(name = "password_hash", length = 500)
        private String passwordHash;

        @Column(name = "full_name", length = 255)
        private String fullName;

        @Column(length = 20)
        private String phone;

        @Column(name = "avatar_url", length = 500)
        private String avatarUrl;

        @Column(name = "is_active", nullable = false)
        private boolean isActive;

        @Column(name = "is_email_verified", nullable = false)
        private boolean isEmailVerified;

        @Column(name = "is_system", nullable = false)
        private boolean isSystem;

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "user_roles", schema = "app", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
        @Builder.Default
        private Set<RoleSchema> roles = new HashSet<>();

        @CreatedBy
        @Column(name = "created_by", length = 100)
        private String createdBy;

        @LastModifiedBy
        @Column(name = "updated_by", length = 100)
        private String updatedBy;

        @CreatedDate
        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        @LastModifiedDate
        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        @Column(name = "deleted_at")
        private LocalDateTime deletedAt;

        public UserSchema(UserAccountModel userAccountModel) {
                this.id = userAccountModel.getId();
                this.tenantId = userAccountModel.getTenantId();
                this.username = userAccountModel.getUsername();
                this.email = userAccountModel.getEmail();
                this.passwordHash = userAccountModel.getPasswordHash();
                this.fullName = userAccountModel.getFullName();
                this.phone = userAccountModel.getPhone();
                this.avatarUrl = userAccountModel.getAvatarUrl();
                this.isActive = userAccountModel.isActive();
                this.isEmailVerified = userAccountModel.isEmailVerified();
                this.isSystem = userAccountModel.isSystem();
                this.createdBy = userAccountModel.getCreatedBy();
                this.updatedBy = userAccountModel.getUpdatedBy();
                this.createdAt = userAccountModel.getCreatedAt();
                this.updatedAt = userAccountModel.getUpdatedAt();
                this.deletedAt = userAccountModel.getDeletedAt();
                if (userAccountModel.getRoles() != null) {
                        this.roles = userAccountModel.getRoles().stream()
                                        .map(RoleSchema::new)
                                        .collect(java.util.stream.Collectors.toSet());
                }
        }

        public UserAccountModel toUserAccountModel() {
                Set<com.kompu.api.entity.role.model.RoleModel> roleModels = this.roles != null
                                ? this.roles.stream()
                                                .map(RoleSchema::toRoleModel)
                                                .collect(java.util.stream.Collectors.toSet())
                                : new HashSet<>();

                return UserAccountModel.builder()
                                .id(this.id)
                                .tenantId(this.tenantId)
                                .username(this.username)
                                .email(this.email)
                                .passwordHash(this.passwordHash)
                                .fullName(this.fullName)
                                .phone(this.phone)
                                .avatarUrl(this.avatarUrl)
                                .isActive(this.isActive)
                                .isEmailVerified(this.isEmailVerified)
                                .isSystem(this.isSystem)
                                .roles(roleModels)
                                .createdBy(this.createdBy)
                                .updatedBy(this.updatedBy)
                                .createdAt(this.createdAt)
                                .updatedAt(this.updatedAt)
                                .deletedAt(this.deletedAt)
                                .build();
        }

}
