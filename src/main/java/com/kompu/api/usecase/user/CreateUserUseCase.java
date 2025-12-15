package com.kompu.api.usecase.user;

import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.kompu.api.entity.user.gateway.UserGateway;
import com.kompu.api.entity.user.model.UserAccountModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateUserUseCase {

    private final UserGateway userGateway;
    private final BCryptPasswordEncoder passwordEncoder;

    public CreateUserUseCase(UserGateway userGateway, BCryptPasswordEncoder passwordEncoder) {
        this.userGateway = userGateway;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user account with the provided details.
     * Password is automatically hashed using BCrypt.
     *
     * @param email       the email address
     * @param rawPassword the plain-text password to be hashed
     * @param fullName    the user's full name
     * @param tenantId    the tenant ID (for multi-tenant system)
     * @return the created UserAccountModel
     */
    public UserAccountModel createUser(String email, String rawPassword,
            String fullName, UUID tenantId) {
        log.info("Creating new user account for email: {} for tenant: {}", email, tenantId);

        UserAccountModel newUser = UserAccountModel.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .isActive(true)
                .isEmailVerified(false)
                .isSystem(false)
                .build();

        UserAccountModel savedUser = userGateway.create(newUser);
        log.info("User account created successfully: {} (ID: {})", email, savedUser.getId());
        return savedUser;
    }

}
