package com.kompu.api.usecase.usertoken;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kompu.api.entity.usertoken.gateway.UserSessionGateway;
import com.kompu.api.entity.usertoken.model.UserSessionModel;
import com.kompu.api.entity.user.model.UserAccountModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateUserSessionUseCase {

    private final UserSessionGateway userSessionGateway;

    public CreateUserSessionUseCase(UserSessionGateway userSessionGateway) {
        this.userSessionGateway = userSessionGateway;
    }

    /**
     * Creates a new user session with the provided details.
     *
     * @param userAccount the UserAccountModel
     * @param ipAddress   the client IP address
     * @param userAgent   the client user agent string
     * @return the created UserSessionModel
     */
    public UserSessionModel createSession(UserAccountModel userAccount, String ipAddress, String userAgent) {
        log.info("Creating new session for user: {} from IP: {}", userAccount.getEmail(), ipAddress);

        LocalDateTime now = LocalDateTime.now();
        UserSessionModel session = UserSessionModel.builder()
                .id(UUID.randomUUID())
                .tenantId(userAccount.getTenantId())
                .userId(userAccount.getId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(now)
                .lastActiveAt(now)
                .isActive(true)
                .deletedAt(null)
                .build();

        UserSessionModel savedSession = userSessionGateway.create(session);
        log.info("User session created successfully with ID: {}", savedSession.getId());

        return savedSession;
    }

}
