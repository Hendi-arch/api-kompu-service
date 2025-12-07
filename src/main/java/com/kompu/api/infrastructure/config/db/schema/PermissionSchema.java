package com.kompu.api.infrastructure.config.db.schema;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kompu.api.entity.permission.model.PermissionModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "permissions", schema = "app", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
public class PermissionSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(length = 500)
    private String description;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public PermissionSchema(PermissionModel permissionModel) {
        this.id = permissionModel.getId();
        this.code = permissionModel.getCode();
        this.description = permissionModel.getDescription();
        this.createdAt = permissionModel.getCreatedAt();
    }

    public PermissionModel toPermissionModel() {
        return PermissionModel.builder()
                .id(this.id)
                .code(this.code)
                .description(this.description)
                .createdAt(this.createdAt)
                .build();
    }

}
