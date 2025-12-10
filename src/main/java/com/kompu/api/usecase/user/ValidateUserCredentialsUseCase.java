package com.kompu.api.usecase.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.kompu.api.entity.user.exception.PasswordNotMatchException;
import com.kompu.api.entity.user.exception.UserNotFoundException;
import com.kompu.api.entity.user.gateway.UserGateway;
import com.kompu.api.entity.user.model.UserAccountModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateUserCredentialsUseCase {

    private final UserGateway userGateway;
    private final BCryptPasswordEncoder passwordEncoder;

    public ValidateUserCredentialsUseCase(UserGateway userGateway, BCryptPasswordEncoder passwordEncoder) {
        this.userGateway = userGateway;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Validates user credentials (username and password).
     * Throws UserNotFoundException if user not found by username.
     * Throws PasswordNotMatchException if password doesn't match.
     *
     * @param username    the username to validate
     * @param rawPassword the plain-text password to validate
     * @return UserAccountModel if credentials are valid
     */
    public UserAccountModel validateCredentials(String username, String rawPassword) {
        UserAccountModel user = userGateway.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("Password mismatch for user: {}", username);
            throw new PasswordNotMatchException();
        }

        if (!user.isActive()) {
            log.warn("User account is inactive: {}", username);
            throw new UserNotFoundException();
        }

        return user;
    }

}
