package com.kompu.api.infrastructure.config.db.schema;

import java.util.UUID;

import com.kompu.api.entity.role.model.RolePermissionModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@IdClass(RolePermissionId.class)
@Table(name = "role_permissions", schema = "app")
public class RolePermissionSchema {

    @Id
    @Column(name = "role_id")
    private UUID roleId;

    @Id
    @Column(name = "permission_id")
    private UUID permissionId;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_role_permission_role_id"))
    private RoleSchema role;

    @ManyToOne
    @JoinColumn(name = "permission_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_role_permission_permission_id"))
    private PermissionSchema permission;

    public RolePermissionSchema(RolePermissionModel rolePermissionModel) {
        this.roleId = rolePermissionModel.getRoleId();
        this.permissionId = rolePermissionModel.getPermissionId();
    }

    public RolePermissionModel toRolePermissionModel() {
        return RolePermissionModel.builder()
                .roleId(this.roleId)
                .permissionId(this.permissionId)
                .role(this.role != null ? this.role.toRoleModel() : null)
                .permission(this.permission != null ? this.permission.toPermissionModel() : null)
                .build();
    }

}
