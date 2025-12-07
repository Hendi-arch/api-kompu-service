package com.kompu.api.entity.user.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.kompu.api.entity.AbstractEntity;
import com.kompu.api.entity.userrole.model.UserRoleModel;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class UserAccountModel extends AbstractEntity<Long> {

    public UserAccountModel(
            String username,
            String password,
            BigDecimal balance,
            UserRoleModel role) {
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.role = role;
    }

    private String username;

    private String password;

    private BigDecimal balance;

    private UserRoleModel role;

    private String jwtToken;

    private LocalDateTime jwtExpiryDateTime;

    private String createdBy;

    private String updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
