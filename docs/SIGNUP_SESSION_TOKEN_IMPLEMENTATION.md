# SignUp Use Case: Session & Refresh Token Integration

## Overview

The `SignUpUseCase` has been enhanced to integrate `UserSessionGateway` and `RefreshTokenGateway` into the user registration workflow. This ensures that newly registered users have active sessions and refresh tokens initialized immediately after signup, enabling seamless token-based authentication without requiring an immediate login.

---

## Architecture & Design

### Gateway Pattern Integration

```
SignUpUseCase
├── UserSessionGateway (creates app.user_sessions records)
├── RefreshTokenGateway (creates app.refresh_tokens records)
└── [Other gateways for user, role, member, tenant]
```

### Database Schema Alignment

The implementation directly maps to the database schema defined in `initial_07122025.sql` (lines 339-378):

#### User Sessions Table (`app.user_sessions`)

```sql
CREATE TABLE app.user_sessions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid,                    -- NULL for super admin
  user_id uuid NOT NULL,             -- REFERENCES app.users(id)
  ip inet,                           -- Client IP address
  user_agent text,                   -- Client User-Agent
  created_at timestamptz DEFAULT now(),
  last_active_at timestamptz DEFAULT now(),
  is_active boolean DEFAULT true,
  deleted_at timestamptz
);
```

#### Refresh Tokens Table (`app.refresh_tokens`)

```sql
CREATE TABLE app.refresh_tokens (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,             -- REFERENCES app.users(id)
  session_id uuid,                   -- REFERENCES app.user_sessions(id)
  token_hash bytea NOT NULL,         -- Hash of the actual token
  created_at timestamptz DEFAULT now(),
  expires_at timestamptz NOT NULL,
  revoked_at timestamptz,
  CONSTRAINT ux_refresh_user_hash UNIQUE (user_id, token_hash)
);
```

---

## Implementation Details

### 1. Dual Execute Methods

The `SignUpUseCase` now provides two execution paths:

#### Path A: Basic Signup (No Session Context)

```java
public UserAccountModel execute(ISignUpRequest request)
```

- Used when signup doesn't have HTTP request context
- Delegates to the overloaded method with `null` IP and User-Agent
- Completes without session creation

#### Path B: Web-Based Signup (With Session Context)

```java
public UserAccountModel execute(ISignUpRequest request, String ipAddress, String userAgent)
```

- Used when signup is initiated from an HTTP request
- Accepts optional `ipAddress` and `userAgent` parameters
- Creates user session and refresh token if context is provided
- Controllers should extract these from `HttpServletRequest`

### 2. Signup Workflow (8 Steps)

```
Step 1: Validate Signup Request
        ↓
Step 2: Generate Tenant ID
        ↓
Step 3: Create User Account
        ↓
Step 4: Assign User Role (System role)
        ↓
Step 5: Create Member Record
        ↓
Step 6: Create Tenant (with user as founder)
        ↓
Step 7: Setup Tenant Domain
        ↓
Step 8: Create User Session & Refresh Token ← NEW
        ↓
    Complete & Return
```

### 3. Session Creation Flow

```java
createUserSession(newUser, tenantId, ipAddress, userAgent)
  ├─ Build UserSessionModel
  │  ├─ id: UUID.randomUUID()
  │  ├─ tenantId: <signup tenant>
  │  ├─ userId: <created user>
  │  ├─ ipAddress: <client IP (optional)>
  │  ├─ userAgent: <client UA (optional)>
  │  ├─ createdAt: now()
  │  ├─ lastActiveAt: now()
  │  └─ isActive: true
  │
  ├─ Persist via UserSessionGateway.create()
  │
  └─ Create Initial Refresh Token
     ├─ Build RefreshTokenModel
     │  ├─ id: UUID.randomUUID()
     │  ├─ userId: <created user>
     │  ├─ sessionId: <created session>
     │  ├─ tokenHash: UUID-based placeholder
     │  ├─ createdAt: now()
     │  └─ expiresAt: now() + 30 days
     │
     └─ Persist via RefreshTokenGateway.create()
```

---

## Key Features

### 1. Optional Session Creation

- Sessions are created **only if** IP address or User-Agent is provided
- Non-blocking: If session creation fails, signup continues
- Error logged but exception not thrown

### 2. Refresh Token Management

- **Validity Period**: 30 days (configurable via `REFRESH_TOKEN_VALIDITY_DAYS`)
- **Token Hash**: UUID-based placeholder (real token generation in security layer)
- **Session Binding**: Refresh token linked to user session for device tracking
- **Expiration**: Auto-calculated as `now() + 30 days`

### 3. Transactional Integrity

- Entire signup process wrapped in `@Transactional`
- Session/token creation fails gracefully without affecting core signup
- Database consistency maintained across all operations

### 4. Logging & Observability

```java
log.info("User session created for user: {}", newUser.getUsername());
log.debug("User session created with ID: {} for user: {} from IP: {}",
    createdSession.getId(), user.getUsername(), ipAddress);
log.debug("Initial refresh token created for session: {} for user: {} with expiration: {}",
    session.getId(), user.getUsername(), expiresAt);
```

---

## Usage Examples

### Example 1: Web Controller (with HTTP context)

```java
@PostMapping("/signup")
public ResponseEntity<?> signup(
        @RequestBody SignUpRequest request,
        HttpServletRequest httpRequest) {

    String ipAddress = getClientIp(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    // Pass session context to use case
    UserAccountModel user = signUpUseCase.execute(
        request,
        ipAddress,
        userAgent
    );

    return ResponseEntity.ok(user);
}

private String getClientIp(HttpServletRequest request) {
    String xForwarded = request.getHeader("X-Forwarded-For");
    if (xForwarded != null && !xForwarded.isEmpty()) {
        return xForwarded.split(",")[0];
    }
    return request.getRemoteAddr();
}
```

### Example 2: Batch/API Signup (without HTTP context)

```java
public void batchSignUp(List<SignUpRequest> requests) {
    for (SignUpRequest request : requests) {
        // No session context - sessions not created
        UserAccountModel user = signUpUseCase.execute(request);
        log.info("Batch signup completed for user: {}", user.getUsername());
    }
}
```

---

## Integration with Security Layer

### Token Generation Pipeline

1. **Signup Use Case**: Creates refresh token record in database

   - Stores token hash and expiration
   - Links to user session

2. **Authentication Handler** (`MyAuthenticationHandler`):

   - Generates actual JWT token string
   - Signs with RSA key pair

3. **Token Validation** (`SecurityMethodFilter`):

   - Validates JWT signature
   - Checks token revocation status
   - Validates against refresh token records

4. **Session Management**:
   - Tracks last_active_at via user session
   - Supports device-based token revocation
   - Enables "logout all devices" functionality

### Security Architecture Alignment

The implementation integrates with existing security components:

- **JwtUtils**: Token validation and claims extraction
- **SecurityMethodFilter**: JWT validation in request pipeline
- **RevokedJwtTokenFilter**: Token revocation checks
- **CorsSecurityFilter**: Request origin validation
- **MyUserDetailService**: User details lookup

---

## Constants & Configuration

```java
private static final String TENANT_ROLE_ID = "10000000-0000-0000-0000-000000000002";
private static final String MEMBER_STATUS_ACTIVE = "active";
private static final String EMPTY_METADATA = "{}";
private static final int MEMBER_CODE_MAX_SEQUENCE = 99999;
private static final long REFRESH_TOKEN_VALIDITY_DAYS = 30;
```

### Configurable Values

- **REFRESH_TOKEN_VALIDITY_DAYS**: Default 30 days, adjustable per security policy
- **TENANT_ROLE_ID**: System role assigned to new users (currently hardcoded to System role)

---

## Error Handling Strategy

### Critical Failures (Block Signup)

- Invalid signup request
- User creation failure
- Role assignment failure
- Tenant creation failure

### Non-Critical Failures (Log & Continue)

- Session creation error
- Refresh token creation error

```java
try {
    createUserSession(newUser, tenantId, ipAddress, userAgent);
} catch (Exception e) {
    log.error("Error creating user session during signup for user: {}. "
        + "Signup will continue but user will need to login to get tokens.",
        user.getUsername(), e);
    // Non-critical operation; log error but do not throw exception
}
```

---

## Testing Considerations

### Unit Test Scenarios

1. **Session Creation Success**

   - Verify session created with correct fields
   - Verify refresh token created with correct expiration
   - Verify session linked to user and tenant

2. **Session Creation Failure**

   - Verify signup completes even if session creation fails
   - Verify error logged appropriately

3. **No Session Context**
   - Verify signup completes without session when IP/UA null
   - Verify no session records created

### Integration Test Scenarios

1. **Full Signup Flow**

   - Create user → Create session → Create refresh token
   - Verify all records in database

2. **Session Validity**

   - Verify is_active = true
   - Verify last_active_at = created_at
   - Verify deleted_at is null

3. **Token Lifecycle**
   - Verify token not revoked at signup
   - Verify expiration date correct
   - Verify token hash unique per user+session

---

## Database Constraints Enforced

From `initial_07122025.sql`:

1. **User Sessions**

   - `user_id` REFERENCES `app.users(id)` ON DELETE CASCADE
   - `session_id` REFERENCES `app.user_sessions(id)` ON DELETE SET NULL
   - Index on `(user_id)` for fast lookup

2. **Refresh Tokens**

   - `user_id` REFERENCES `app.users(id)` ON DELETE CASCADE
   - `session_id` REFERENCES `app.user_sessions(id)` ON DELETE SET NULL
   - UNIQUE constraint on `(user_id, token_hash)` prevents duplicate tokens
   - Index on `(token_hash)` for fast validation

3. **Tenant Isolation**
   - user_sessions.tenant_id tracks multi-tenancy
   - Enables session-based access control

---

## Performance Considerations

### Database Operations (Within Signup Transaction)

- 1 INSERT into `app.user_sessions`
- 1 INSERT into `app.refresh_tokens`
- Total overhead: ~2 additional database calls per signup

### Indexes Utilized

- `idx_user_sessions_user` for user lookup
- `idx_refresh_tokens_user` for token lookup
- `idx_refresh_tokens_hash` for token validation

### Optimization Opportunities

- Batch token creation if multiple tokens needed
- Cache active sessions in Redis for high-throughput scenarios
- Async session creation for non-blocking signup (future enhancement)

---

## Relationship to Other Components

### CreateTenantUseCase

- Called with actual user ID (available after user creation)
- Tenant associated with founder_user_id

### SetupTenantDomainUseCase

- Called after tenant creation
- Initializes domain routing for multi-tenant isolation

### RoleGateway & UserRoleGateway

- Assigns System role (ID: 10000000-0000-0000-0000-000000000002)
- New users get system-assigned role with basic permissions

### MemberGateway

- Creates corresponding member record
- Tracks member metadata and status

---

## Future Enhancements

1. **Email Verification Link**

   - Include refresh token in email verification flow
   - Require email verification before token activation

2. **Session Tracking**

   - Log session creation in auth_audit table
   - Track device fingerprints for fraud detection

3. **Configurable Token Expiry**

   - Per-tenant token validity settings
   - Different expiry for web vs mobile

4. **Async Session Creation**

   - Non-blocking session/token creation
   - Improved signup latency

5. **Multi-Device Support**
   - Create multiple sessions for different devices
   - Implement "login on multiple devices" feature

---

## Summary

The SignUpUseCase now provides a complete user onboarding experience:

1. **Creates user account** with role assignment
2. **Creates member record** for organizational tracking
3. **Creates tenant** with domain configuration
4. **Creates session** for device tracking (optional)
5. **Creates refresh token** for stateless authentication

This implementation aligns with the multi-tenant architecture and existing security infrastructure while maintaining clean code separation between business logic (use case) and infrastructure (gateways).
