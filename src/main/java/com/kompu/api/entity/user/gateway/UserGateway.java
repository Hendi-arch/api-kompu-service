package com.kompu.api.entity.user.gateway;

import java.util.Optional;
import java.math.BigDecimal;
import java.util.List;

import com.kompu.api.entity.user.exception.UserNotFoundException;
import com.kompu.api.entity.user.model.UserAccountModel;

public interface UserGateway {

    UserAccountModel create(UserAccountModel userAccountModel);

    UserAccountModel update(UserAccountModel userAccountModel);

    void delete(Long id) throws UserNotFoundException;

    Optional<UserAccountModel> findById(Long id);

    Optional<UserAccountModel> findByUsername(String username);

    List<UserAccountModel> findAll();

    boolean hasSufficientBalanceForTransaction(Long senderId, BigDecimal transactionAmount);

}
