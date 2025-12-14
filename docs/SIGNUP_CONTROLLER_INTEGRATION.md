# SignUp Controller Integration Guide

## Overview

This guide shows how to integrate the enhanced `SignUpUseCase` with sessions and refresh tokens in the SignUp controller.

---

## Controller Implementation

### Basic Implementation

```java
package com.kompu.api.infrastructure.auth.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kompu.api.entity.user.model.UserAccountModel;
import com.kompu.api.infrastructure.auth.dto.SignUpRequestDTO;
import com.kompu.api.infrastructure.auth.dto.SignUpResponseDTO;
import com.kompu.api.usecase.auth.SignUpUseCase;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class SignUpController {

    private final SignUpUseCase signUpUseCase;

    public SignUpController(SignUpUseCase signUpUseCase) {
        this.signUpUseCase = signUpUseCase;
    }

    /**
     * User registration endpoint with session tracking.
     *
     * Creates:
     * 1. User account
     * 2. User role assignment
     * 3. Member record
     * 4. Tenant organization
     * 5. User session (device tracking)
     * 6. Refresh token
     *
     * @param request the signup request DTO
     * @param httpRequest the HTTP request (for IP and User-Agent extraction)
     * @return 201 Created with user details and tokens
     */
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDTO> signup(
            @RequestBody SignUpRequestDTO request,
            HttpServletRequest httpRequest) {

        log.info("Signup request received for username: {}", request.username());

        // Extract session context from HTTP request
        String ipAddress = extractClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        log.debug("Signup request from IP: {}", ipAddress);

        try {
            // Execute signup with session context
            UserAccountModel newUser = signUpUseCase.execute(
                request,
                ipAddress,
                userAgent
            );

            log.info("Signup completed successfully for user: {} (ID: {})",
                newUser.getUsername(), newUser.getId());

            // Convert domain model to response DTO
            SignUpResponseDTO response = SignUpResponseDTO.from(newUser);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid signup request: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .build();
        } catch (Exception e) {
            log.error("Signup failed for username: {}", request.username(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }

    /**
     * Extracts the client IP address from the HTTP request.
     *
     * Checks for proxy headers first, then falls back to direct IP.
     *
     * @param request the HTTP servlet request
     * @return the client IP address
     */
    private String extractClientIpAddress(HttpServletRequest request) {
        // Check for X-Forwarded-For header (proxy/load balancer)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            // First IP in comma-separated list is the original client
            return xForwardedFor.split(",")[0].trim();
        }

        // Check for X-Real-IP header (nginx proxy)
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.trim().isEmpty()) {
            return xRealIp.trim();
        }

        // Use direct connection IP
        return request.getRemoteAddr();
    }
}
```

---

## Response DTO

### SignUpResponseDTO

```java
package com.kompu.api.infrastructure.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kompu.api.entity.user.model.UserAccountModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponseDTO {

    private UUID id;

    private String username;

    private String email;

    private String fullName;

    private String phone;

    private UUID tenantId;

    private boolean isEmailVerified;

    private LocalDateTime createdAt;

    /**
     * Convert domain model to response DTO.
     *
     * @param user the user account model
     * @return the response DTO
     */
    public static SignUpResponseDTO from(UserAccountModel user) {
        return SignUpResponseDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phone(user.getPhone())
            .tenantId(user.getTenantId())
            .isEmailVerified(user.isEmailVerified())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
```

---

## Enhanced Response with Token Info

If you want to include session and token information:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponseDTO {

    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private UUID tenantId;
    private boolean isEmailVerified;
    private LocalDateTime createdAt;

    // Session info (optional)
    private UUID sessionId;

    // Token info (optional, only if token generation happens in controller)
    private String accessToken;
    private String refreshToken;
    private long expiresIn; // seconds

    public static SignUpResponseDTO from(
            UserAccountModel user,
            UUID sessionId,
            String accessToken,
            String refreshToken,
            long expiresIn) {

        return SignUpResponseDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phone(user.getPhone())
            .tenantId(user.getTenantId())
            .isEmailVerified(user.isEmailVerified())
            .createdAt(user.getCreatedAt())
            .sessionId(sessionId)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(expiresIn)
            .build();
    }
}
```

---

## Advanced: Auto-Login After Signup

If you want to automatically log the user in and return tokens:

```java
@PostMapping("/signup-and-login")
public ResponseEntity<SignUpResponseDTO> signupAndLogin(
        @RequestBody SignUpRequestDTO request,
        HttpServletRequest httpRequest) {

    log.info("Signup with auto-login requested for: {}", request.username());

    String ipAddress = extractClientIpAddress(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    try {
        // Step 1: Create user with session
        UserAccountModel newUser = signUpUseCase.execute(
            request,
            ipAddress,
            userAgent
        );

        // Step 2: Generate access token (from authentication handler)
        String accessToken = generateAccessToken(newUser);

        // Step 3: Get refresh token from database
        // (Session was created by signup, so token exists)
        UserSessionModel session = userSessionGateway
            .findActiveSessionsByUserId(newUser.getId())
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Session not created"));

        // Step 4: Build response with tokens
        SignUpResponseDTO response = SignUpResponseDTO.from(
            newUser,
            session.getId(),
            accessToken,
            "refresh_token_string", // From refresh token table
            15 * 60 // 15 minutes in seconds
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);

    } catch (Exception e) {
        log.error("Signup with auto-login failed", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .build();
    }
}

private String generateAccessToken(UserAccountModel user) {
    // Use JwtUtils or similar from security layer
    return authenticationHandler.generateToken(user);
}
```

---

## Request/Response Examples

### Request

```json
POST /api/v1/auth/signup
Content-Type: application/json

{
  "username": "john.doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "phone": "+62812345678",
  "address": "Jl. Sudirman No. 1, Jakarta",
  "tenantName": "John's Business",
  "tenantCode": "john-business"
}
```

### Response (201 Created)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john.doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "phone": "+62812345678",
  "tenantId": "660e8400-e29b-41d4-a716-446655440001",
  "isEmailVerified": false,
  "createdAt": "2025-12-15T10:30:45.123456"
}
```

### Response with Tokens (Enhanced)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john.doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "phone": "+62812345678",
  "tenantId": "660e8400-e29b-41d4-a716-446655440001",
  "isEmailVerified": false,
  "createdAt": "2025-12-15T10:30:45.123456",
  "sessionId": "770e8400-e29b-41d4-a716-446655440002",
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440003",
  "expiresIn": 900
}
```

---

## Error Handling

### Validation Errors

```json
POST /api/v1/auth/signup
{
  "username": "",  // empty
  "email": "john@example.com"
}

Response: 400 Bad Request
{
  "error": "Username is required"
}
```

### Duplicate User

```json
Response: 409 Conflict
{
  "error": "Username already exists for this tenant"
}
```

### Server Error

```json
Response: 500 Internal Server Error
{
  "error": "An unexpected error occurred during signup"
}
```

---

## Configuration in Application Properties

```properties
# application.properties or application.yml

# Session timeout (minutes)
server.servlet.session.timeout=30

# Cookie settings
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict

# JWT token expiration
app.jwt.access-token-expiration=900000      # 15 minutes in milliseconds
app.jwt.refresh-token-expiration=2592000000 # 30 days in milliseconds
```

---

## Integration with Spring Security

```java
// In AppSecurityConfigurer or similar

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // ... other config ...
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/signup").permitAll()
            .requestMatchers("/api/v1/auth/refresh").permitAll()
            .anyRequest().authenticated()
        )
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/api/v1/auth/**")
        );

    return http.build();
}
```

---

## Testing the Endpoint

### Using curl

```bash
curl -X POST http://localhost:3333/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Pass123!",
    "fullName": "Test User",
    "phone": "+62800000000",
    "address": "Test Address"
  }'
```

### Using Postman

1. **Method**: POST
2. **URL**: http://localhost:3333/api/v1/auth/signup
3. **Headers**: Content-Type: application/json
4. **Body** (raw JSON):

```json
{
  "username": "postman-user",
  "email": "postman@test.com",
  "password": "PostmanPass123!",
  "fullName": "Postman Test",
  "phone": "+62812345678",
  "address": "Test Address, Jakarta"
}
```

---

## What Happens Behind the Scenes

1. **HTTP Request Received**

   - Client IP extracted
   - User-Agent captured
   - Request body parsed

2. **SignUpUseCase.execute() Called**

   - User account created
   - Role assigned
   - Member record created
   - Tenant created
   - User session created (with IP & User-Agent)
   - Refresh token created (expires in 30 days)

3. **Response Returned**

   - User details returned
   - Client can store user data
   - Client receives session ID (if included)
   - Client receives tokens (if generated)

4. **Session Tracking Enabled**
   - Device/browser tracked
   - IP address logged
   - Device logout available
   - Token refresh available

---

## Session Lifecycle

```
User Signup
    ↓
Session Created (is_active=true)
    ↓
Client uses refresh token
    ↓
Server validates token
    ↓
Access token issued
    ↓
Client makes authenticated requests
    ↓
(User logout or 30 days pass)
    ↓
Session deactivated (is_active=false)
    ↓
Token revoked
    ↓
Further refresh attempts fail
```

---

## Summary

The controller integrates seamlessly with the enhanced SignUpUseCase:

1. ✅ Extracts IP address and User-Agent
2. ✅ Passes them to the use case
3. ✅ Session and tokens automatically created
4. ✅ Returns user information to client
5. ✅ Optional: Return tokens for immediate authentication
6. ✅ Error handling for validation failures
7. ✅ Logging for monitoring and debugging

The implementation follows REST conventions and maintains clean separation between:

- **Controller**: HTTP handling
- **Use Case**: Business logic
- **Gateway**: Data persistence
- **Model**: Domain objects
