package com.kompu.api.usecase.usertoken;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.kompu.api.infrastructure.config.web.security.util.JwtUtils;
import com.kompu.api.entity.user.model.UserAccountModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateAccessTokenUseCase {

    private final JwtUtils jwtUtils;

    public GenerateAccessTokenUseCase(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * Generates a new JWT access token for the given user.
     *
     * @param userAccount the UserAccountModel
     * @return the generated JWT token
     */
    public String generateAccessToken(UserAccountModel userAccount) {
        log.info("Generating access token for user: {}", userAccount.getEmail());

        // Convert UserAccountModel to Spring's UserDetails for JWT generation
        UserDetails userDetails = User.builder()
                .username(userAccount.getId().toString())
                .password(userAccount.getPasswordHash())
                .authorities("ROLE_USER")
                .build();

        String token = jwtUtils.generateJwtToken(userDetails);
        log.info("Access token generated successfully for user: {}", userAccount.getEmail());

        return token;
    }

}
