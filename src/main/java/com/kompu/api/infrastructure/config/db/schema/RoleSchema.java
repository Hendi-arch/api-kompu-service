package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.role.model.RoleModel;

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
@Table(name = "roles", schema = "app", uniqueConstraints = @UniqueConstraint(columnNames = { "tenant_id", "name" }))
public class RoleSchema {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @Column(name = "tenant_id")
        private UUID tenantId;

        @Column(nullable = false, length = 100)
        private String name;

        @Column(length = 500)
        private String description;

        @Column(name = "is_system", nullable = false)
        private boolean isSystem;

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "role_permissions", schema = "app", joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id"))
        @Builder.Default
        private Set<PermissionSchema> permissions = new HashSet<>();

        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        @LastModifiedDate
        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        public RoleSchema(RoleModel roleModel) {
                this.id = roleModel.getId();
                this.tenantId = roleModel.getTenantId();
                this.name = roleModel.getName();
                this.description = roleModel.getDescription();
                this.isSystem = roleModel.isSystem();
                this.createdAt = roleModel.getCreatedAt();
                this.updatedAt = roleModel.getUpdatedAt();
                if (roleModel.getPermissions() != null) {
                        this.permissions = roleModel.getPermissions().stream()
                                        .map(PermissionSchema::new)
                                        .collect(java.util.stream.Collectors.toSet());
                }
        }

        public RoleModel toRoleModel() {
                Set<com.kompu.api.entity.permission.model.PermissionModel> permissionModels = this.permissions != null
                                ? this.permissions.stream()
                                                .map(PermissionSchema::toPermissionModel)
                                                .collect(java.util.stream.Collectors.toSet())
                                : new HashSet<>();

                return RoleModel.builder()
                                .id(this.id)
                                .tenantId(this.tenantId)
                                .name(this.name)
                                .description(this.description)
                                .isSystem(this.isSystem)
                                .permissions(permissionModels)
                                .createdAt(this.createdAt)
                                .updatedAt(this.updatedAt)
                                .build();
        }

}
