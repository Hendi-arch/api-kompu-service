package com.kompu.api.usecase.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.kompu.api.entity.user.exception.PasswordNotMatchException;
import com.kompu.api.entity.user.exception.UserNotFoundException;
import com.kompu.api.entity.user.gateway.UserGateway;
import com.kompu.api.entity.user.model.UserAccountModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChangePasswordUseCase {

    private final UserGateway userGateway;
    private final BCryptPasswordEncoder passwordEncoder;

    public ChangePasswordUseCase(UserGateway userGateway, BCryptPasswordEncoder passwordEncoder) {
        this.userGateway = userGateway;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Changes the password for an existing user.
     * Validates the old password before allowing the change.
     *
     * @param userId      the user ID
     * @param oldPassword the current plain-text password
     * @param newPassword the new plain-text password
     * @return the updated UserAccountModel
     */
    public UserAccountModel changePassword(java.util.UUID userId, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);

        UserAccountModel user = userGateway.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            log.warn("Old password mismatch for user: {}", userId);
            throw new PasswordNotMatchException();
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        UserAccountModel updatedUser = userGateway.update(user);

        log.info("Password changed successfully for user: {}", userId);
        return updatedUser;
    }

}
