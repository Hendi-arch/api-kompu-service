package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.UUID;

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

import com.kompu.api.entity.user.model.UserRoleModel;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(UserRoleId.class)
@Table(name = "user_roles", schema = "app")
public class UserRoleSchema {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "role_id")
    private UUID roleId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_user_roles_user_id"))
    private UserSchema user;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_user_roles_role_id"))
    private RoleSchema role;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    public UserRoleSchema(UserRoleModel userRoleModel) {
        this.userId = userRoleModel.getUserId();
        this.roleId = userRoleModel.getRoleId();
        this.assignedAt = userRoleModel.getAssignedAt();
    }

    public UserRoleModel toUserRoleModel() {
        return UserRoleModel.builder()
                .userId(this.userId)
                .roleId(this.roleId)
                .user(this.user != null ? this.user.toUserAccountModel() : null)
                .role(this.role != null ? this.role.toRoleModel() : null)
                .assignedAt(this.assignedAt)
                .build();
    }

}
