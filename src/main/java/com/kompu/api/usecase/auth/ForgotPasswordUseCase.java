package com.kompu.api.usecase.auth;

import com.kompu.api.entity.user.gateway.UserGateway;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ForgotPasswordUseCase {

    private final UserGateway userGateway;

    public ForgotPasswordUseCase(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    public void execute(String email) {
        log.info("Processing forgot password for email: {}", email);

        userGateway.findByEmail(email).ifPresent(user -> {
            // TODO: Generate password reset token
            // TODO: Save token to DB (need PasswordResetToken entities)
            // TODO: Send email
            log.info("Sending password reset email to: {}", user.getEmail());
            // For now, we just log that it happened.
        });

        // Security best practice: Don't reveal if user exists.
        log.info("If email exists, password reset instructions have been sent to: {}", email);
    }
}
