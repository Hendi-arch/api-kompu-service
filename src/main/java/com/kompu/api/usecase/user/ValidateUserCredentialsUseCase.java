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
     * Validates user credentials (email and password).
     * Throws UserNotFoundException if user not found by email.
     * Throws PasswordNotMatchException if password doesn't match.
     *
     * @param email       the email to validate
     * @param rawPassword the plain-text password to validate
     * @return UserAccountModel if credentials are valid
     */
    public UserAccountModel validateCredentials(String email, String rawPassword) {
        UserAccountModel user = userGateway.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("Password mismatch for user: {}", email);
            throw new PasswordNotMatchException();
        }

        if (!user.isActive()) {
            log.warn("User account is inactive: {}", email);
            throw new UserNotFoundException();
        }

        return user;
    }

}
