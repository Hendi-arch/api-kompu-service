# Quick Reference: Using SignUpUseCase with Sessions & Tokens

## TL;DR

The `SignUpUseCase` now automatically creates user sessions and refresh tokens when you provide IP address and User-Agent.

---

## Basic Usage (No Session)

```java
// Use case layer - simple signup without session context
UserAccountModel newUser = signUpUseCase.execute(signUpRequest);
```

**Result**: User created, but no session/token records.

---

## Web-Based Signup (With Session)

```java
// Controller layer - signup from HTTP request
@PostMapping("/auth/signup")
public ResponseEntity<?> signup(
        @RequestBody SignUpRequest request,
        HttpServletRequest httpRequest) {

    String ipAddress = httpRequest.getRemoteAddr();
    String userAgent = httpRequest.getHeader("User-Agent");

    UserAccountModel newUser = signUpUseCase.execute(
        request,
        ipAddress,
        userAgent
    );

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new SignUpResponse(newUser));
}
```

**Result**: User created + Session created + Refresh token created.

---

## What Gets Created

### 1. User Session

```
app.user_sessions:
├─ id: <UUID>
├─ tenant_id: <signup tenant>
├─ user_id: <created user>
├─ ip: <client IP>
├─ user_agent: <client browser>
├─ created_at: now()
├─ last_active_at: now()
├─ is_active: true
└─ deleted_at: null
```

### 2. Refresh Token

```
app.refresh_tokens:
├─ id: <UUID>
├─ user_id: <created user>
├─ session_id: <created session>
├─ token_hash: <UUID hash>
├─ created_at: now()
├─ expires_at: now() + 30 days
├─ revoked_at: null
```

---

## Gateway Methods Called

```java
// UserSessionGateway
UserSessionModel session = userSessionGateway.create(sessionModel);

// RefreshTokenGateway
RefreshTokenModel token = refreshTokenGateway.create(tokenModel);
```

Both are injected in SignUpUseCase constructor.

---

## Transaction Handling

- ✅ Entire signup is atomic (`@Transactional`)
- ✅ Session/token failures don't block signup
- ✅ Rollback on critical failures (user/tenant/member)
- ⚠️ Session/token errors logged but not thrown

---

## Error Scenarios

### Session Creation Fails

```
LOG: Error creating user session during signup for user: john.doe
     Signup will continue but user will need to login to get tokens.
RESULT: User created successfully (session skipped)
```

### Signup Fails (Critical)

```
Exception thrown:
- IllegalArgumentException (validation)
- RoleNotFoundException (system role)
- Database constraint violation
RESULT: Rollback entire transaction (nothing created)
```

---

## Configuration

```java
// In SignUpUseCase constants:
private static final long REFRESH_TOKEN_VALIDITY_DAYS = 30;
```

Change the `30` to adjust token expiration (in days).

---

## Database Indexes Used

```sql
-- Fast session lookup
idx_user_sessions_user (user_id)
idx_user_sessions_tenant (tenant_id)

-- Fast token lookup
idx_refresh_tokens_user (user_id)
idx_refresh_tokens_hash (token_hash)
```

---

## Nullable Fields

- **ipAddress**: Optional (null allowed)
- **userAgent**: Optional (null allowed)
- **Session creation**: Only if at least one of above is provided

```java
// This WON'T create a session:
signUpUseCase.execute(request, null, null);

// This WILL create a session:
signUpUseCase.execute(request, "192.168.1.1", null);
```

---

## Integration Points

### After Signup Complete

1. **Issue JWT Access Token**

   - Use user ID and tenant ID
   - Sign with RSA key pair
   - Set short expiration (15-30 min)

2. **Return Refresh Token**

   - Return token to client
   - Client stores in secure cookie/storage
   - Used to obtain new access tokens

3. **Set Session Cookie** (Optional)
   - Use session ID for CSRF protection
   - Track device/browser activity

### On Token Refresh

```java
// Client sends: refreshToken
// Server:
1. Hash the refresh token
2. Lookup in app.refresh_tokens by token_hash
3. Check if revoked_at is null
4. Check if expires_at > now()
5. Check if session is still active
6. Issue new access token
```

---

## Accessing Session Data Later

```java
// Find user's active sessions
List<UserSessionModel> sessions =
    userSessionGateway.findActiveSessionsByUserId(userId);

// Deactivate specific session (logout from device)
userSessionGateway.deactivateSession(sessionId);

// Revoke all tokens for session
List<RefreshTokenModel> tokens =
    refreshTokenGateway.findBySessionId(sessionId);
for (RefreshTokenModel token : tokens) {
    refreshTokenGateway.revokeToken(token.getId());
}
```

---

## Security Notes

1. **Token Hash Storage**

   - Don't store actual tokens in database
   - Hash them using BCrypt or similar
   - Compare on refresh using `MessageDigest.isEqual()`

2. **Expiration Enforcement**

   - Always check `expires_at > LocalDateTime.now()`
   - Remove expired tokens periodically (batch job)

3. **Revocation Check**

   - Always verify `revoked_at == null` before allowing refresh
   - Support immediate revocation (logout)

4. **Session Isolation**
   - Sessions include `tenant_id`
   - Enforce tenant-based access control
   - Support "logout from all devices"

---

## Testing

### Minimal Test

```java
@Test
void testSignupCreatesSession() {
    SignUpRequest request = new SignUpRequest(...);

    UserAccountModel user = signUpUseCase.execute(
        request,
        "192.168.1.100",
        "Mozilla/5.0..."
    );

    // Verify session created
    List<UserSessionModel> sessions =
        userSessionGateway.findByUserId(user.getId());
    assertThat(sessions).hasSize(1);
    assertThat(sessions.get(0).getIpAddress()).isEqualTo("192.168.1.100");

    // Verify token created
    List<RefreshTokenModel> tokens =
        refreshTokenGateway.findByUserId(user.getId());
    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getRevokedAt()).isNull();
}
```

---

## Common Issues

### ❌ Session not created

- **Cause**: Both `ipAddress` and `userAgent` are null
- **Fix**: Pass at least one value: `signUpUseCase.execute(request, ip, ua)`

### ❌ Token expires immediately

- **Cause**: `REFRESH_TOKEN_VALIDITY_DAYS` set to 0
- **Fix**: Set to at least 1: `private static final long REFRESH_TOKEN_VALIDITY_DAYS = 30`

### ❌ Duplicate token hash constraint

- **Cause**: Creating multiple tokens with same hash
- **Fix**: Use UUID to ensure uniqueness: `UUID.randomUUID().toString()`

### ❌ Session constraint violation

- **Cause**: Deleting user without cascade delete
- **Fix**: Ensure cascade rules: `ON DELETE CASCADE` in foreign key

---

## What Changed in SignUpUseCase

```diff
  public UserAccountModel execute(ISignUpRequest request) {
+     return execute(request, null, null);
  }

+ public UserAccountModel execute(ISignUpRequest request, String ipAddress, String userAgent) {
      // ... existing steps ...
+     if (ipAddress != null || userAgent != null) {
+         createUserSession(newUser, tenantId, ipAddress, userAgent);
+     }
  }

+ private void createUserSession(...) { ... }
+ private void createInitialRefreshToken(...) { ... }
```

Three new methods:

1. Overloaded `execute()` method
2. `createUserSession()` method
3. `createInitialRefreshToken()` method

---

## Next Steps

1. **Update SignUpController**

   - Extract IP and User-Agent from HttpServletRequest
   - Pass to use case

2. **Update SignUpResponse DTO**

   - Return refresh token to client
   - Include session ID (optional)

3. **Implement Token Refresh Endpoint**

   - Accept refresh token
   - Validate against database
   - Return new access token

4. **Add Session Logout Endpoint**

   - Accept session ID
   - Call `userSessionGateway.deactivateSession()`
   - Revoke all associated tokens

5. **Add Tests**
   - Session creation tests
   - Token expiration tests
   - Revocation tests
