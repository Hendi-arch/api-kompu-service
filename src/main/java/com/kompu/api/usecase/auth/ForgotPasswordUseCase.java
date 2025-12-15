package com.kompu.api.usecase.auth;

import com.kompu.api.entity.user.gateway.UserGateway;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ForgotPasswordUseCase {

    private final UserGateway userGateway;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JavaMailSender emailSender;

    public ForgotPasswordUseCase(UserGateway userGateway, BCryptPasswordEncoder passwordEncoder,
            JavaMailSender emailSender) {
        this.userGateway = userGateway;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
    }

    public void execute(String email) {
        log.info("Processing forgot password for email: {}", email);

        userGateway.findByEmail(email).ifPresent(user -> {
            // Generate new password
            String newPassword = generateNewPassword(email);
            String newPasswordHash = passwordEncoder.encode(newPassword);

            // Update user
            user.setPasswordHash(newPasswordHash);
            userGateway.update(user);

            // Send email
            sendEmail(user.getEmail(), newPassword);

            log.info("Password reset and email sent for: {}", user.getEmail());
        });

        // Security best practice: Don't reveal if user exists.
        log.info("If email exists, password reset instructions have been sent to: {}", email);
    }

    private String generateNewPassword(String email) {
        // Simple deterministic password for now as requested: email + "123"
        // In production, use a random string generator.
        return email.split("@")[0] + "123";
    }

    private void sendEmail(String to, String newPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@kompu.id");
            message.setTo(to);
            message.setSubject("Your New Password");
            message.setText("Your password has been reset. Your new password is: " + newPassword
                    + "\n\nPlease change it immediately after logging in.");
            emailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            // Don't rethrow to avoid exposing user existence, or handle according to
            // requirements
        }
    }
}
