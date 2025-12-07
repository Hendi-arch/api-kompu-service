package com.kompu.api.entity.userrole.model;

import java.time.LocalDateTime;

import com.kompu.api.entity.AbstractEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class UserRoleModel extends AbstractEntity<Long> {

    public UserRoleModel() {
    }

    private String role;

    private String createdBy;

    private String updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
