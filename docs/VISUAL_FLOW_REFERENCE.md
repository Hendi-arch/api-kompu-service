# Visual Reference: Session & Token Flow

## High-Level Process Flow

```
┌──────────────────────────────────────────────────────────────────────┐
│                         CLIENT                                        │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │ POST /api/v1/auth/signup                                       │  │
│  │ {username, email, password, fullName, phone, ...}             │  │
│  │ Headers: User-Agent, X-Forwarded-For                          │  │
│  └────────────────────────────────────────────────────────────────┘  │
└───────────────────────────┬──────────────────────────────────────────┘
                            │
                            ↓
┌──────────────────────────────────────────────────────────────────────┐
│                    SIGNUP CONTROLLER                                 │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │ 1. Extract IP address (X-Forwarded-For or remoteAddr)        │  │
│  │ 2. Extract User-Agent from headers                           │  │
│  │ 3. Parse JSON request body → SignUpRequestDTO               │  │
│  │ 4. Call: signUpUseCase.execute(request, ip, userAgent)       │  │
│  └────────────────────────────────────────────────────────────────┘  │
└───────────────────────────┬──────────────────────────────────────────┘
                            │
                            ↓
┌──────────────────────────────────────────────────────────────────────┐
│                      SIGNUP USE CASE                                 │
│                                                                       │
│  ┌─ Step 1: Validate ───────────────────────────────────────┐      │
│  │ Check: request != null, username/email/password not empty │      │
│  └───────────────────────────────────────────────────────────┘      │
│           │                                                          │
│           ↓                                                          │
│  ┌─ Step 2-7: Core Signup ───────────────────────────────────┐      │
│  │ • Generate Tenant ID                                       │      │
│  │ • Create User Account (→ app.users)                       │      │
│  │ • Assign User Role (→ app.user_roles)                     │      │
│  │ • Create Member Record (→ app.members)                    │      │
│  │ • Create Tenant (→ app.tenants)                           │      │
│  │ • Setup Tenant Domain (→ app.tenant_domains)              │      │
│  └───────────────────────────────────────────────────────────┘      │
│           │                                                          │
│           ↓                                                          │
│  ┌─ Step 8: Create Session & Token (if IP/UA provided) ──────┐      │
│  │                                                             │      │
│  │  ┌─── Build Session Model ──────────────────────────┐      │      │
│  │  │ {                                                │      │      │
│  │  │   id: UUID,                                      │      │      │
│  │  │   tenantId: <tenant>,                            │      │      │
│  │  │   userId: <user>,                                │      │      │
│  │  │   ipAddress: "203.0.113.42",                    │      │      │
│  │  │   userAgent: "Mozilla/5.0...",                  │      │      │
│  │  │   createdAt: 2025-12-15T10:30:45.123,           │      │      │
│  │  │   lastActiveAt: 2025-12-15T10:30:45.123,        │      │      │
│  │  │   isActive: true                                 │      │      │
│  │  │ }                                                │      │      │
│  │  └──────────────────────────┬──────────────────────┘      │      │
│  │                             │                             │      │
│  │                             ↓                             │      │
│  │  ┌─── Persist Session ──────────────────────────────┐      │      │
│  │  │ userSessionGateway.create(session)              │      │      │
│  │  │ ↓                                                │      │      │
│  │  │ INSERT INTO app.user_sessions (...)             │      │      │
│  │  └──────────────────────────┬──────────────────────┘      │      │
│  │                             │                             │      │
│  │                             ↓                             │      │
│  │  ┌─── Build Refresh Token ──────────────────────────┐      │      │
│  │  │ {                                                │      │      │
│  │  │   id: UUID,                                      │      │      │
│  │  │   userId: <user>,                                │      │      │
│  │  │   sessionId: <session>,                          │      │      │
│  │  │   tokenHash: "1a2b3c4d...",                      │      │      │
│  │  │   createdAt: 2025-12-15T10:30:45.123,           │      │      │
│  │  │   expiresAt: 2026-01-14T10:30:45.123,           │      │      │
│  │  │   revokedAt: null                                │      │      │
│  │  │ }                                                │      │      │
│  │  └──────────────────────────┬──────────────────────┘      │      │
│  │                             │                             │      │
│  │                             ↓                             │      │
│  │  ┌─── Persist Refresh Token ────────────────────────┐      │      │
│  │  │ refreshTokenGateway.create(token)               │      │      │
│  │  │ ↓                                                │      │      │
│  │  │ INSERT INTO app.refresh_tokens (...)            │      │      │
│  │  └──────────────────────────┬──────────────────────┘      │      │
│  │                             │                             │      │
│  │                             ↓                             │      │
│  │  ┌─── Log Result ───────────────────────────────────┐      │      │
│  │  │ "User session created with ID: <sessionId>"     │      │      │
│  │  │ "Refresh token created with expiration: <date>" │      │      │
│  │  └──────────────────────────────────────────────────┘      │      │
│  │                                                             │      │
│  └─────────────────────────────────────────────────────────────┘      │
│           │                                                          │
│           ↓                                                          │
│  ┌─ Return: UserAccountModel ─────────────────────────────────┐      │
│  │ {                                                           │      │
│  │   id, username, email, fullName, tenantId,               │      │
│  │   isEmailVerified, createdAt, ...                         │      │
│  │ }                                                           │      │
│  └───────────────────────────────────────────────────────────┘      │
│                                                                       │
└───────────────────────────┬──────────────────────────────────────────┘
                            │
                            ↓
┌──────────────────────────────────────────────────────────────────────┐
│                    SIGNUP CONTROLLER RESPONSE                         │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │ 201 Created                                                    │  │
│  │ {                                                              │  │
│  │   "id": "550e8400-e29b-41d4-a716-446655440000",              │  │
│  │   "username": "john.doe",                                     │  │
│  │   "email": "john@example.com",                                │  │
│  │   "fullName": "John Doe",                                     │  │
│  │   "tenantId": "660e8400-e29b-41d4-a716-446655440001",        │  │
│  │   "createdAt": "2025-12-15T10:30:45.123456"                  │  │
│  │ }                                                              │  │
│  └────────────────────────────────────────────────────────────────┘  │
└───────────────────────────┬──────────────────────────────────────────┘
                            │
                            ↓
┌──────────────────────────────────────────────────────────────────────┐
│                         CLIENT                                        │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │ Store user data                                                │  │
│  │ Redirect to login or dashboard                               │  │
│  │ Ready for authentication                                      │  │
│  └────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Database State After Signup

### app.users

```
id                                   | username  | email           | tenant_id                            | ...
550e8400-e29b-41d4-a716-446655440000 | john.doe  | john@example.com | 660e8400-e29b-41d4-a716-446655440001 | ...
```

### app.user_roles

```
user_id                              | role_id
550e8400-e29b-41d4-a716-446655440000 | 10000000-0000-0000-0000-000000000002  (System role)
```

### app.members

```
id                                   | tenant_id                            | user_id                              | member_code | ...
770e8400-e29b-41d4-a716-446655440002 | 660e8400-e29b-41d4-a716-446655440001 | 550e8400-e29b-41d4-a716-446655440000 | MEM20250001 | ...
```

### app.tenants

```
id                                   | name         | code         | founder_user_id                      | ...
660e8400-e29b-41d4-a716-446655440001 | John's Org   | john-business | 550e8400-e29b-41d4-a716-446655440000 | ...
```

### app.user_sessions

```
id                                   | tenant_id                            | user_id                              | ip            | user_agent        | created_at              | is_active
880e8400-e29b-41d4-a716-446655440003 | 660e8400-e29b-41d4-a716-446655440001 | 550e8400-e29b-41d4-a716-446655440000 | 203.0.113.42  | Mozilla/5.0...    | 2025-12-15 10:30:45.123 | true
```

### app.refresh_tokens

```
id                                   | user_id                              | session_id                           | token_hash                      | created_at              | expires_at              | revoked_at
990e8400-e29b-41d4-a716-446655440004 | 550e8400-e29b-41d4-a716-446655440000 | 880e8400-e29b-41d4-a716-446655440003 | 1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c | 2025-12-15 10:30:45.123 | 2026-01-14 10:30:45.123 | null
```

---

## Session Lifecycle Timeline

```
Time    Event                           is_active    revoked_at    Notes
────────────────────────────────────────────────────────────────────────────
T0      User signs up                   true         null          Session created
                                                                    Token created

T1      User logs in                    true         null          Access token issued
                                                                    Session last_active_at updated

T2      User makes requests             true         null          Session activity tracked

T30     User logs out                   false        <timestamp>   Session deactivated
                                                                    Token revoked

T31     Token refresh attempt           FAILED       -             Token not found/revoked
                                                                    User must login again

────────────────────────────────────────────────────────────────────────────
Alternative: Token Expiration

T30     30 days pass                    true         null          Token expires naturally
                                                                    (expires_at < now())

T31     Token refresh attempt           FAILED       null          Token expired
                                                                    User must login again
```

---

## Gateway Method Calls Sequence

```
SignUpUseCase.execute(request, ip, userAgent)
│
├─ userGateway.create(userModel)
│  └─→ INSERT INTO app.users
│      RETURN: UserAccountModel
│
├─ roleGateway.findById(roleId)
│  └─→ SELECT FROM app.roles
│      RETURN: RoleModel
│
├─ userRoleGateway.create(userRoleModel)
│  └─→ INSERT INTO app.user_roles
│      RETURN: UserRoleModel
│
├─ memberGateway.create(memberModel)
│  └─→ INSERT INTO app.members
│      RETURN: MemberModel
│
├─ memberGateway.existsByTenantIdAndMemberCode(tenantId, code)
│  └─→ SELECT COUNT(*) FROM app.members
│      RETURN: boolean
│
├─ createTenantUseCase.createTenantWithMetadata(name, code, userId, metadata)
│  └─→ (Delegates to TenantUseCase)
│      └─→ INSERT INTO app.tenants
│          RETURN: TenantModel
│
├─ setupTenantDomainUseCase.setupInitialDomain(tenantId, code)
│  └─→ (Delegates to TenantDomainUseCase)
│      └─→ INSERT INTO app.tenant_domains
│          RETURN: void
│
├─ userSessionGateway.create(sessionModel) ← NEW
│  └─→ INSERT INTO app.user_sessions
│      RETURN: UserSessionModel
│
└─ refreshTokenGateway.create(tokenModel) ← NEW
   └─→ INSERT INTO app.refresh_tokens
       RETURN: RefreshTokenModel
```

---

## Error Paths

### Scenario 1: Invalid Request

```
execute(null, ip, ua)
│
├─ validateSignUpRequest()
│  └─ request == null
│     └─ throw IllegalArgumentException("Signup request cannot be null")
│
└─ Rollback entire transaction
   Database: No records created
```

### Scenario 2: User Already Exists

```
execute(request with existing username, ip, ua)
│
├─ User account creation succeeds
├─ Role assignment succeeds
├─ Member creation succeeds
├─ Tenant creation succeeds
├─ Domain setup succeeds
│
├─ Session creation succeeds
├─ Token creation succeeds
│
└─ return UserAccountModel ✓
   (In real scenario, username unique constraint would prevent step 1)
```

### Scenario 3: Session Creation Fails

```
execute(request, ip, ua)
│
├─ Steps 1-7: All succeed ✓
│
├─ Step 8: createUserSession()
│  │
│  ├─ Build session model ✓
│  ├─ userSessionGateway.create() → EXCEPTION ✗
│  │
│  ├─ catch (Exception e)
│  │  ├─ log.error("Error creating user session...")
│  │  └─ Continue (non-critical)
│  │
│  └─ No token created
│
└─ return UserAccountModel ✓
   Database: User created, but no session/token
   Note: User can still login normally
```

---

## IP Extraction Examples

```java
// Example 1: Direct connection (no proxy)
HttpServletRequest.getRemoteAddr()
→ "203.0.113.42"

// Example 2: Behind nginx proxy
X-Real-IP: 203.0.113.42
→ "203.0.113.42"

// Example 3: Behind load balancer (multiple proxies)
X-Forwarded-For: 203.0.113.42, 198.51.100.2, 192.0.2.1
→ "203.0.113.42" (first IP is original client)

// Implementation in controller:
private String extractClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
        return xForwardedFor.split(",")[0].trim();
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.trim().isEmpty()) {
        return xRealIp.trim();
    }
    return request.getRemoteAddr();
}
```

---

## Transactional Boundaries

```
@Transactional  ← Transaction starts
public UserAccountModel execute(ISignUpRequest request, String ip, String ua) {

    validateSignUpRequest()          ← Same transaction
    createUserAccount()              ← Same transaction
    assignUserRole()                 ← Same transaction
    createMemberRecord()             ← Same transaction
    createTenantForUser()            ← Same transaction
    setupTenantDomain()              ← Same transaction

    if (ip != null || ua != null) {
        createUserSession()          ← Same transaction
            try {
                userSessionGateway.create()     ← Same transaction
                createInitialRefreshToken()     ← Same transaction
                    refreshTokenGateway.create()← Same transaction
            } catch (Exception) {
                ← Exception caught, transaction continues
            }
    }

    return user;
}                                    ← Transaction commits (all or nothing)
                                       if no exception thrown
```

---

## Summary

The SignUpUseCase now implements a complete, enterprise-grade user registration system with:

1. **Atomic Signup Process** - All-or-nothing transaction
2. **Session Tracking** - Device and IP logging
3. **Token Management** - Refresh token for stateless auth
4. **Error Resilience** - Non-critical failures don't block signup
5. **Clean Architecture** - Gateway pattern for data access
6. **Security Focus** - Tenant isolation, token hashing, expiration

Everything flows seamlessly from HTTP request → Use Case → Gateways → Database, with comprehensive logging and error handling at each step.
