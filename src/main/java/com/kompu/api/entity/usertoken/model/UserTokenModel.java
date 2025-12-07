package com.kompu.api.entity.usertoken.model;

import java.time.LocalDateTime;

import com.kompu.api.entity.AbstractEntity;
import com.kompu.api.entity.user.model.UserAccountModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserTokenModel extends AbstractEntity<Long> {

    public UserTokenModel(
            UserAccountModel userAccount,
            String token,
            LocalDateTime expiryDateTime) {
        this.userAccount = userAccount;
        this.token = token;
        this.expiryDateTime = expiryDateTime;
    }

    private UserAccountModel userAccount;

    private String token;

    private LocalDateTime expiryDateTime;

    private String createdBy;

    private String updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
