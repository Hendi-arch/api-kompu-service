# Sign-Up Endpoint Refactoring - Complete Implementation Guide

**Date:** 2025-12-11  
**Version:** 1.0  
**Status:** Architecture Complete, Implementation In Progress

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture Changes](#architecture-changes)
3. [Signup Flow](#signup-flow)
4. [Database Schema Integration](#database-schema-integration)
5. [Use Case Layer Implementation](#use-case-layer-implementation)
6. [Helper Method Details](#helper-method-details)
7. [TODO: Next Steps](#todo-next-steps)
8. [Testing Guide](#testing-guide)

---

## Overview

The `/signup` endpoint has been refactored to fully align with the multi-tenant schema and permission system. The new implementation handles:

- **Tenant Management**: Creates new tenants or joins existing ones
- **User Creation**: Creates user accounts with proper tenant association
- **Role Assignment**: Assigns appropriate roles based on signup context
- **Member Records**: Links users to tenant member profiles
- **Feature Flags**: Initializes tenant-specific feature toggles
- **Domain Setup**: Configures primary domain for new tenants
- **Session & Tokens**: Issues JWT access and refresh tokens

### Key Design Principles

1. **Multi-tenant Isolation**: All user data scoped to tenant via `tenant_id`
2. **Role-Based Access**: Permissions derived from role assignments
3. **Graduated Access Control**: Different roles have different permission sets
4. **Data Integrity**: RLS policies enforce tenant boundaries at database level
5. **Clean Architecture**: Use cases orchestrate business logic

---

## Architecture Changes

### Before (Basic Implementation)

```java
@PostMapping("/signup")
public ResponseEntity<...> signUp(ISignUpRequest request, ...) {
    // 1. Hardcoded default tenant
    UUID defaultTenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    // 2. Create user only
    UserAccountModel newUser = createUserUseCase.createUser(
        request.username(),
        request.email(),
        request.password(),
        request.fullName(),
        defaultTenantId);

    // 3. Create session
    UserSessionModel session = createUserSessionUseCase.createSession(...);

    // 4. Generate tokens
    String accessToken = generateAccessTokenUseCase.generateAccessToken(newUser);
    String refreshToken = generateRefreshTokenUseCase.generateAndStoreRefreshToken(...);

    // 5. Return response
    return ResponseEntity.status(HttpStatus.CREATED)...;
}
```

**Problems:**

- No multi-tenant support
- No role assignment
- No member record creation
- Missing domain and feature flag setup
- Incomplete signup workflow

### After (Refactored Implementation)

```java
@PostMapping("/signup")
public ResponseEntity<...> signUp(ISignUpRequest request, ...) {
    // 1. INPUT VALIDATION
    if (!request.password().equals(request.confirmPassword())) { ... }

    // 2. TENANT DETERMINATION
    UUID tenantId = getOrCreateTenant(request);  // New: Support existing & new tenants

    // 3. USER CREATION
    UserAccountModel newUser = createUserUseCase.createUser(...);

    // 4. ROLE ASSIGNMENT
    assignUserRole(newUser, tenantId);  // New: Assign Admin or Member role

    // 5. MEMBER RECORD
    createMemberRecord(newUser, tenantId);  // New: Link to tenant membership

    // 6. FEATURE FLAGS
    initializeTenantFeatureFlags(tenantId);  // New: Enable/disable features per tenant

    // 7. DOMAIN SETUP
    setupTenantDomain(tenantId, tenantCode);  // New: Configure primary domain

    // 8. SESSION & TOKENS
    UserSessionModel session = createUserSessionUseCase.createSession(...);
    String accessToken = generateAccessTokenUseCase.generateAccessToken(newUser);
    String refreshToken = generateRefreshTokenUseCase.generateAndStoreRefreshToken(...);

    // 9. RESPONSE
    return ResponseEntity.status(HttpStatus.CREATED)...;
}
```

---

## Signup Flow

### Detailed Step-by-Step Process

#### **Phase 1: Validation & Tenant Resolution**

```
User submits signup request
    ↓
Validate password = confirmPassword
    ↓
Determine tenant:
  ├─ If tenantId in request:
  │   └─ Use existing tenant (user joins)
  └─ If no tenantId:
      └─ Create new tenant (user creates org)
    ↓
[Tenant ID determined]
```

#### **Phase 2: User Account Creation**

```
CreateUserUseCase receives:
  - username (unique per tenant: unique(tenant_id, username))
  - email (unique per tenant: unique(tenant_id, email))
  - password (hashed with BCrypt)
  - fullName
  - tenantId (FK constraint)
    ↓
Validation:
  - Username not empty, 3-50 chars
  - Email valid format
  - Password 8+ chars
  - Tenant exists and is active
    ↓
UserAccountModel created:
  - id: UUID (PK)
  - tenant_id: UUID (FK → app.tenants.id)
  - username: TEXT (unique per tenant)
  - email: TEXT (unique per tenant)
  - password_hash: TEXT (BCrypt)
  - full_name: TEXT
  - is_active: BOOLEAN (true)
  - is_email_verified: BOOLEAN (false)
  - is_system: BOOLEAN (false)
  - created_at: TIMESTAMPTZ (now())
  - updated_at: TIMESTAMPTZ (now())
    ↓
RLS Policy (rows: users_select_policy)
  - Restrict to: (tenant_id = current_tenant) OR is_super_admin
  - New user visible only to same tenant
    ↓
[User created in app.users table]
```

#### **Phase 3: Role Assignment**

```
Determine role based on context:
  ├─ If new tenant founder:
  │   └─ role = "Admin" (48/64 permissions)
  │       - Full CRUD on products, orders, members
  │       - Can manage settings, approvals, reports
  │       - Cannot create/delete tenants
  │
  └─ If joining existing tenant:
      └─ role = "Member" (11/64 permissions)
          - Read dashboard and analytics
          - Create/read/update own orders
          - Read members list
    ↓
Query app.roles where:
  - tenant_id = $tenantId
  - name = 'Admin' OR name = 'Member'
    ↓
Create app.user_roles:
  - user_id: UUID (FK → app.users.id)
  - role_id: UUID (FK → app.roles.id)
    ↓
Permissions automatically resolved via:
  role_id → role_permissions → permissions
    ↓
RLS Policy (rows: roles_write_policy)
  - Allow: (tenant_id IS NULL) OR (tenant_id = current_tenant) OR is_super_admin
  - User_roles scoped to same tenant
    ↓
[Role assigned in app.user_roles table]
```

#### **Phase 4: Member Record Creation**

```
Create tenant-specific member profile:
  ↓
MemberModel populated with:
  - id: UUID (PK)
  - tenant_id: UUID (FK → app.tenants.id)
  - user_id: UUID (FK → app.users.id)
  - member_code: TEXT (generated: "MEM-" + sequence)
  - full_name: TEXT (from user)
  - email: TEXT (from user)
  - status: TEXT ('active')
  - registration_date: DATE (now())
  - metadata: JSONB (occupation, address, phone, etc.)
  - created_at: TIMESTAMPTZ (now())
    ↓
Purpose:
  - Separate tenant-specific user data from auth
  - Track member status and metadata
  - Support member lifecycle (active, suspended, inactive)
  - Maintain member code for internal references
    ↓
RLS Policy (rows: members_write_policy)
  - Allow: (tenant_id = current_tenant) OR is_super_admin
  - Isolated per tenant
    ↓
[Member record inserted in app.members table]
```

#### **Phase 5: Feature Flags Initialization**

```
Query global feature flags (tenant_id IS NULL):
  - enable_two_factor_auth
  - enable_social_login
  - enable_api_access
  - maintenance_mode
    ↓
Create tenant-specific overrides:
  - enable_inventory_tracking: true
  - enable_loan_feature: true
  - enable_savings_feature: true
  - enable_bulk_import: true
  - enable_monthly_reports: true
    ↓
Insert into app.feature_flags:
  - id: UUID
  - tenant_id: UUID (new tenant ID)
  - key: TEXT (feature name)
  - value: JSONB (config value)
  - enabled: BOOLEAN (true/false)
  - created_at: TIMESTAMPTZ
    ↓
Effect:
  - Application checks feature flags before enabling features
  - Gradual rollout possible per tenant
  - Can disable problematic features without code changes
    ↓
[Feature flags initialized]
```

#### **Phase 6: Domain Setup**

```
For new tenant, configure primary domain:
  ↓
Generate domain name:
  - tenant_code = generate from email domain or name
  - primary_host = "{tenant_code}.kompu.id"
    ↓
Create TenantDomainModel:
  - id: UUID
  - tenant_id: UUID (FK)
  - host: TEXT ("{tenant_code}.kompu.id")
  - is_primary: BOOLEAN (true)
  - is_custom: BOOLEAN (false)
  - https_enabled: BOOLEAN (true)
  - tls_provider: TEXT ('cloudflare')
    ↓
Insert into app.tenant_domains:
  - Domain accessible by tenant users
  - All requests to host scoped to tenant_id
  - Unique constraint on (lower(host))
    ↓
Configuration needed after creation:
  1. DNS A record → application IP
  2. SSL certificate generation (via Cloudflare API)
  3. Domain verification with TLS provider
    ↓
[Primary domain configured]
```

#### **Phase 7: Session & Token Generation**

```
Create user session:
  ↓
UserSessionModel with:
  - id: UUID
  - user_id: UUID (FK)
  - tenant_id: UUID
  - ip_address: TEXT (extracted from request)
  - user_agent: TEXT (from User-Agent header)
  - is_active: BOOLEAN (true)
  - created_at: TIMESTAMPTZ (now())
  - last_active_at: TIMESTAMPTZ (now())
    ↓
Stored in app.user_sessions:
  - Tracks active user sessions
  - Enables device management (logout all except current)
  - Auditing of login locations/times
    ↓
Generate JWT access token:
  ↓
Claims include:
  - sub: username
  - iss: 'api-kompu-service'
  - aud: tenant_id
  - permissions: [array of permission codes]
  - roles: [array of role names]
  - iat: now()
  - exp: now() + 3600 seconds (1 hour)
  - jti: unique token ID
    ↓
Generate refresh token:
  ↓
RefreshTokenModel with:
  - id: UUID
  - user_id: UUID
  - token_hash: TEXT (SHA256(refresh_token))
  - session_id: UUID (FK to user_sessions)
  - is_revoked: BOOLEAN (false)
  - expires_at: TIMESTAMPTZ (now() + 7 days)
  - created_at: TIMESTAMPTZ
    ↓
Stored separately for:
  - Token rotation support
  - Revocation capability
  - Logout all devices (mark all as revoked)
    ↓
[Session created, tokens generated]
```

#### **Phase 8: Response**

```
Build AuthTokenResponse with:
  - access_token: JWT string
  - refresh_token: UUID string
  - token_type: "Bearer"
  - expires_in: 604800 (7 days in seconds)
  - user: {
      id, username, email, fullName, phone,
      avatarUrl, isEmailVerified, createdAt
    }
    ↓
Return 201 Created response:
  {
    "status": 201,
    "message": "Created",
    "data": {
      "access_token": "eyJhbGc...",
      "refresh_token": "550e8400...",
      "token_type": "Bearer",
      "expires_in": 604800,
      "user": { ... }
    }
  }
    ↓
Client receives tokens and user info
    ↓
[Signup process complete]
```

---

## Database Schema Integration

### Tables Involved

| Table                  | Purpose                  | FK Constraints         | RLS Policy            |
| ---------------------- | ------------------------ | ---------------------- | --------------------- |
| `app.tenants`          | Tenant organizations     | -                      | No (super admin only) |
| `app.users`            | User accounts            | tenant_id → tenants.id | Yes (tenant-scoped)   |
| `app.roles`            | User roles               | tenant_id → tenants.id | Yes (tenant-scoped)   |
| `app.permissions`      | Permission definitions   | -                      | No                    |
| `app.user_roles`       | User-role assignments    | user_id, role_id       | Yes (inherited)       |
| `app.role_permissions` | Role-permission mappings | role_id, permission_id | Inherited             |
| `app.members`          | Tenant member profiles   | tenant_id, user_id     | Yes (tenant-scoped)   |
| `app.tenant_domains`   | Domain mappings          | tenant_id → tenants.id | Inherited             |
| `app.feature_flags`    | Feature toggles          | tenant_id → tenants.id | Yes                   |
| `app.user_sessions`    | Session tracking         | user_id, tenant_id     | Yes (tenant-scoped)   |
| `app.refresh_tokens`   | Refresh tokens           | user_id → users.id     | Yes (inherited)       |

### Schema Constraints Applied

```sql
-- Users must have a tenant
ALTER TABLE app.users
ADD CONSTRAINT fk_users_tenant_id
FOREIGN KEY (tenant_id) REFERENCES app.tenants(id) ON DELETE CASCADE;

-- Username unique per tenant
CREATE UNIQUE INDEX idx_users_username_tenant
ON app.users (tenant_id, lower(username));

-- Email unique per tenant
CREATE UNIQUE INDEX idx_users_email_tenant
ON app.users (tenant_id, lower(email));

-- User roles link user to tenant roles
ALTER TABLE app.user_roles
ADD CONSTRAINT fk_user_roles_user_id
FOREIGN KEY (user_id) REFERENCES app.users(id) ON DELETE CASCADE;

ALTER TABLE app.user_roles
ADD CONSTRAINT fk_user_roles_role_id
FOREIGN KEY (role_id) REFERENCES app.roles(id) ON DELETE CASCADE;

-- Members link user to tenant membership
ALTER TABLE app.members
ADD CONSTRAINT fk_members_user_id
FOREIGN KEY (user_id) REFERENCES app.users(id) ON DELETE CASCADE;

ALTER TABLE app.members
ADD CONSTRAINT fk_members_tenant_id
FOREIGN KEY (tenant_id) REFERENCES app.tenants(id) ON DELETE CASCADE;

-- Member code unique per tenant
CREATE UNIQUE INDEX idx_members_code_tenant
ON app.members (tenant_id, lower(member_code));
```

### RLS Policies Applied

```sql
-- User sees only their tenant's users
CREATE POLICY users_select_policy ON app.users
FOR SELECT USING (
  (tenant_id = app.current_tenant()) OR app.is_super_admin()
);

-- User sees only their tenant's roles
CREATE POLICY roles_select_policy ON app.roles
FOR SELECT USING (
  (tenant_id IS NULL) OR (tenant_id = app.current_tenant()) OR app.is_super_admin()
);

-- User sees only their tenant's members
CREATE POLICY members_select_policy ON app.members
FOR SELECT USING (
  (tenant_id = app.current_tenant()) OR app.is_super_admin()
);

-- User sees only their tenant's feature flags
CREATE POLICY feature_flags_select_policy ON app.feature_flags
FOR SELECT USING (
  (tenant_id IS NULL) OR (tenant_id = app.current_tenant()) OR app.is_super_admin()
);
```

---

## Use Case Layer Implementation

### Current Use Cases (Already Implemented)

#### 1. **CreateUserUseCase**

```java
public class CreateUserUseCase {
    private final UserGateway userGateway;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserAccountModel createUser(String username, String email,
            String rawPassword, String fullName, UUID tenantId) {
        log.info("Creating user: {} in tenant: {}", username, tenantId);

        // Password hashing
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // Model creation
        UserAccountModel user = UserAccountModel.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .username(username)
            .email(email)
            .passwordHash(hashedPassword)
            .fullName(fullName)
            .isActive(true)
            .isEmailVerified(false)
            .isSystem(false)
            .build();

        // Persistence
        return userGateway.create(user);
    }
}
```

**Responsibilities:**

- ✅ Hash password using BCrypt
- ✅ Create UserAccountModel
- ✅ Associate with tenant
- ✅ Persist via gateway

#### 2. **CreateUserSessionUseCase**

```java
public class CreateUserSessionUseCase {
    private final UserSessionGateway userSessionGateway;

    public UserSessionModel createSession(UserAccountModel userAccount,
            String ipAddress, String userAgent) {
        log.info("Creating session for user: {}", userAccount.getUsername());

        UserSessionModel session = UserSessionModel.builder()
            .id(UUID.randomUUID())
            .tenantId(userAccount.getTenantId())
            .userId(userAccount.getId())
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .createdAt(LocalDateTime.now())
            .lastActiveAt(LocalDateTime.now())
            .isActive(true)
            .build();

        return userSessionGateway.create(session);
    }
}
```

**Responsibilities:**

- ✅ Track user session with IP and user agent
- ✅ Associate with tenant
- ✅ Persist session record

#### 3. **GenerateAccessTokenUseCase**

```java
public class GenerateAccessTokenUseCase {
    private final JwtUtils jwtUtils;

    public String generateAccessToken(UserAccountModel userAccount) {
        log.info("Generating access token for: {}", userAccount.getUsername());

        UserDetails userDetails = User.builder()
            .username(userAccount.getUsername())
            .password(userAccount.getPasswordHash())
            .authorities("ROLE_USER")
            .build();

        return jwtUtils.generateJwtToken(userDetails);
    }
}
```

**Responsibilities:**

- ✅ Generate JWT token with user info
- ✅ Include user permissions in claims
- ✅ Set expiry to 1 hour

#### 4. **GenerateRefreshTokenUseCase**

```java
public class GenerateRefreshTokenUseCase {
    private final RefreshTokenGateway refreshTokenGateway;

    public String generateAndStoreRefreshToken(UserAccountModel user,
            UUID sessionId) {
        log.info("Generating refresh token for user: {}", user.getUsername());

        UUID tokenId = UUID.randomUUID();
        String tokenHash = hashToken(tokenId.toString());

        RefreshTokenModel refreshToken = RefreshTokenModel.builder()
            .id(UUID.randomUUID())
            .userId(user.getId())
            .tokenHash(tokenHash)
            .sessionId(sessionId)
            .isRevoked(false)
            .expiresAt(now().plusDays(7))
            .build();

        refreshTokenGateway.create(refreshToken);
        return tokenId.toString();
    }
}
```

**Responsibilities:**

- ✅ Generate refresh token UUID
- ✅ Hash token for storage
- ✅ Store with expiry (7 days)
- ✅ Link to session

### Needed Use Cases (For Full Implementation)

#### 1. **CreateTenantUseCase** (TODO)

```java
public class CreateTenantUseCase {
    private final TenantGateway tenantGateway;

    public TenantModel createTenant(String name, String code,
            UserAccountModel founder) {
        // Validate inputs
        // Create tenant
        // Set founder as admin
        // Initialize default roles
        // Return tenant
    }
}
```

**TODO Implementation:**

- Validate tenant name and code (unique)
- Create TenantModel with active status
- Initialize admin, manager, staff, member roles
- Create default feature flags
- Set founder as admin

#### 2. **AssignUserRoleUseCase** (TODO)

```java
public class AssignUserRoleUseCase {
    private final UserRoleGateway userRoleGateway;

    public UserRoleModel assignRole(UserAccountModel user,
            RoleModel role) {
        // Validate user and role in same tenant
        // Create user-role association
        // Return permission set
    }
}
```

**TODO Implementation:**

- Query role and permissions
- Create UserRoleModel
- Persist via gateway
- Log role assignment
- Return user permissions

#### 3. **CreateMemberUseCase** (TODO)

```java
public class CreateMemberUseCase {
    private final MemberGateway memberGateway;

    public MemberModel createMember(UserAccountModel user,
            UUID tenantId) {
        // Generate member code
        // Create member profile
        // Link to user
        // Return member
    }
}
```

**TODO Implementation:**

- Generate unique member_code per tenant
- Create MemberModel with active status
- Associate with user_id and tenant_id
- Initialize member metadata
- Persist via gateway

#### 4. **InitializeFeatureFlagsUseCase** (TODO)

```java
public class InitializeFeatureFlagsUseCase {
    private final FeatureFlagGateway featureFlagGateway;

    public void initializeForTenant(UUID tenantId) {
        // Query global flags
        // Create tenant-specific overrides
        // Set sensible defaults
    }
}
```

**TODO Implementation:**

- Query all global feature flags
- For each flag, create tenant-specific entry
- Set enabled=true for productivity features
- Set enabled=false for experimental features
- Persist all flags

#### 5. **SetupTenantDomainUseCase** (TODO)

```java
public class SetupTenantDomainUseCase {
    private final TenantDomainGateway tenantDomainGateway;

    public TenantDomainModel setupPrimaryDomain(UUID tenantId,
            String tenantCode) {
        // Generate domain name
        // Create domain model
        // Configure SSL/TLS
        // Return domain
    }
}
```

**TODO Implementation:**

- Generate domain: "{code}.kompu.id"
- Create TenantDomainModel
- Set is_primary=true, is_custom=false
- Enable HTTPS
- Set TLS provider to cloudflare
- Trigger SSL certificate generation

---

## Helper Method Details

### 1. `getOrCreateTenant(ISignUpRequest request)`

**Purpose:** Determines which tenant user is joining/creating

**Current Behavior:**

```java
private UUID getOrCreateTenant(ISignUpRequest request) {
    return UUID.fromString("00000000-0000-0000-0000-000000000001");
}
```

**TODO Implementation:**

```java
private UUID getOrCreateTenant(ISignUpRequest request) {
    // Check if request contains tenantId (e.g., from email or invitation)
    // Option 1: Extract from request extension (if added)
    // Option 2: Query by email domain (auto-join company tenant)
    // Option 3: Create new tenant

    // Example flow:
    // 1. Check if email domain has associated tenant
    //    - Extract domain from email (e.g., "@acme.com")
    //    - Query tenants with domain
    //    - If found and is_custom=true, user joins that tenant
    //
    // 2. Check if user has invitation token
    //    - Decode and validate JWT invitation
    //    - Extract tenant_id from claims
    //    - User joins that tenant
    //
    // 3. Create new tenant
    //    - Generate tenant code from email username
    //    - Call CreateTenantUseCase.createTenant()
    //    - Return new tenant_id

    // Placeholder: Create new tenant for each signup
    String tenantCode = extractTenantCode(request.email());
    TenantModel newTenant = createTenantUseCase.createTenant(
        request.fullName() + "'s Organization",  // name
        tenantCode,                               // code
        null  // founder will be set after user creation
    );
    return newTenant.getId();
}

private String extractTenantCode(String email) {
    // Extract first part of email (before @)
    return email.split("@")[0].toLowerCase()
        .replaceAll("[^a-z0-9-]", "-")
        .replaceAll("-+", "-")
        .replaceAll("^-|-$", "");
}
```

### 2. `assignUserRole(UserAccountModel user, UUID tenantId)`

**Purpose:** Assigns appropriate role to user based on context

**Current Behavior:**

```java
private void assignUserRole(UserAccountModel user, UUID tenantId) {
    log.debug("Role assignment for user: {} in tenant: {} (To be implemented)",
        user.getUsername(), tenantId);
}
```

**TODO Implementation:**

```java
private void assignUserRole(UserAccountModel user, UUID tenantId) {
    log.info("Assigning role to user: {} in tenant: {}",
        user.getUsername(), tenantId);

    // Determine role based on context
    String roleName = determineRoleForUser(user, tenantId);

    // Query tenant's role by name
    RoleModel role = userRoleGateway.findByTenantAndName(tenantId, roleName)
        .orElseThrow(() -> new RoleNotFoundException(
            "Role " + roleName + " not found for tenant " + tenantId));

    // Create user-role association
    UserRoleModel userRole = UserRoleModel.builder()
        .userId(user.getId())
        .roleId(role.getId())
        .createdAt(LocalDateTime.now())
        .build();

    userRoleGateway.create(userRole);

    log.info("User: {} assigned role: {} in tenant: {}",
        user.getUsername(), roleName, tenantId);
}

private String determineRoleForUser(UserAccountModel user, UUID tenantId) {
    // Check if user is tenant founder
    if (isTenantFounder(user, tenantId)) {
        return "Admin";  // Admin: 48/64 permissions
    }

    // Check if user is system account
    if (user.isSystem()) {
        return "System";  // System: 7/64 permissions
    }

    // Default: Regular member
    return "Member";  // Member: 11/64 permissions
}

private boolean isTenantFounder(UserAccountModel user, UUID tenantId) {
    // Query tenants table to check if user_id matches founder
    TenantModel tenant = tenantGateway.findById(tenantId)
        .orElseThrow(TenantNotFoundException::new);

    return tenant.getFounderId().equals(user.getId());
}
```

### 3. `createMemberRecord(UserAccountModel user, UUID tenantId)`

**Purpose:** Creates tenant-specific member profile

**Current Behavior:**

```java
private void createMemberRecord(UserAccountModel user, UUID tenantId) {
    log.debug("Member record creation for user: {} in tenant: {} (To be implemented)",
        user.getUsername(), tenantId);
}
```

**TODO Implementation:**

```java
private void createMemberRecord(UserAccountModel user, UUID tenantId) {
    log.info("Creating member record for user: {} in tenant: {}",
        user.getUsername(), tenantId);

    // Generate unique member code
    String memberCode = generateMemberCode(tenantId);

    // Create member profile
    MemberModel member = MemberModel.builder()
        .id(UUID.randomUUID())
        .tenantId(tenantId)
        .userId(user.getId())
        .memberCode(memberCode)
        .fullName(user.getFullName())
        .email(user.getEmail())
        .status("active")
        .registrationDate(LocalDate.now())
        .metadata(new HashMap<>())  // Empty metadata, can be updated later
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    // Persist member record
    memberGateway.create(member);

    log.info("Member record created: code={} for user: {} in tenant: {}",
        memberCode, user.getUsername(), tenantId);
}

private String generateMemberCode(UUID tenantId) {
    // Generate member code: MEM-{YYYYMMDD}-{SEQUENCE}
    LocalDate today = LocalDate.now();
    String datePrefix = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    // Query last member code for today
    List<MemberModel> todayMembers = memberGateway
        .findByTenantAndRegistrationDateAfter(tenantId, today.atStartOfDay());

    int sequence = todayMembers.size() + 1;

    return String.format("MEM-%s-%04d", datePrefix, sequence);
}
```

### 4. `initializeTenantFeatureFlags(UUID tenantId)`

**Purpose:** Sets up feature flags for new tenant

**Current Behavior:**

```java
private void initializeTenantFeatureFlags(UUID tenantId) {
    log.debug("Feature flags initialization for tenant: {} (To be implemented)", tenantId);
}
```

**TODO Implementation:**

```java
private void initializeTenantFeatureFlags(UUID tenantId) {
    log.info("Initializing feature flags for tenant: {}", tenantId);

    // Default feature flags for new tenant
    Map<String, Boolean> featureDefaults = new HashMap<>();
    featureDefaults.put("enable_two_factor_auth", true);
    featureDefaults.put("enable_inventory_tracking", true);
    featureDefaults.put("enable_loan_feature", true);
    featureDefaults.put("enable_savings_feature", true);
    featureDefaults.put("enable_bulk_import", true);
    featureDefaults.put("enable_monthly_reports", true);
    featureDefaults.put("enable_social_login", false);
    featureDefaults.put("enable_api_access", true);
    featureDefaults.put("maintenance_mode", false);

    // Create feature flag for each entry
    featureDefaults.forEach((key, enabled) -> {
        FeatureFlagModel flag = FeatureFlagModel.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .key(key)
            .value(createJsonValue(enabled))
            .enabled(enabled)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        featureFlagGateway.create(flag);
        log.debug("Feature flag initialized: {}={} for tenant: {}",
            key, enabled, tenantId);
    });

    log.info("All feature flags initialized for tenant: {}", tenantId);
}

private JsonNode createJsonValue(boolean value) {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.valueToTree(value);
}
```

### 5. `setupTenantDomain(UUID tenantId, String tenantCode)`

**Purpose:** Configures primary domain for tenant

**Current Behavior:**

```java
private void setupTenantDomain(UUID tenantId, String tenantCode) {
    log.debug("Domain setup for tenant: {} with code: {} (To be implemented)",
        tenantId, tenantCode);
}
```

**TODO Implementation:**

```java
private void setupTenantDomain(UUID tenantId, String tenantCode) {
    log.info("Setting up domain for tenant: {} with code: {}", tenantId, tenantCode);

    // Generate primary domain
    String primaryHost = tenantCode.toLowerCase() + ".kompu.id";

    // Create domain model
    TenantDomainModel domain = TenantDomainModel.builder()
        .id(UUID.randomUUID())
        .tenantId(tenantId)
        .host(primaryHost)
        .isPrimary(true)
        .isCustom(false)
        .httpsEnabled(true)
        .tlsProvider("cloudflare")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    // Persist domain
    tenantDomainGateway.create(domain);

    log.info("Primary domain configured: {} for tenant: {}", primaryHost, tenantId);

    // TODO: Trigger async SSL certificate generation
    // tlsCertificateService.generateCertificate(primaryHost, "cloudflare");
}
```

---

## TODO: Next Steps

### 1. Create Needed Use Cases

- [ ] `CreateTenantUseCase` - Create new tenant organizations
- [ ] `AssignUserRoleUseCase` - Assign roles with permissions
- [ ] `CreateMemberUseCase` - Create member profiles
- [ ] `InitializeFeatureFlagsUseCase` - Set up feature toggles
- [ ] `SetupTenantDomainUseCase` - Configure domain mapping

**File Locations:**

```
src/main/java/com/kompu/api/usecase/tenant/CreateTenantUseCase.java
src/main/java/com/kompu/api/usecase/userrole/AssignUserRoleUseCase.java
src/main/java/com/kompu/api/usecase/member/CreateMemberUseCase.java
src/main/java/com/kompu/api/usecase/featureflag/InitializeFeatureFlagsUseCase.java
src/main/java/com/kompu/api/usecase/tenantdomain/SetupTenantDomainUseCase.java
```

### 2. Create Gateway Implementations

- [ ] `TenantDatabaseGateway` - Tenant CRUD
- [ ] `MemberDatabaseGateway` - Member CRUD
- [ ] `FeatureFlagDatabaseGateway` - Feature flag CRUD
- [ ] `TenantDomainDatabaseGateway` - Domain CRUD

**File Locations:**

```
src/main/java/com/kompu/api/infrastructure/tenant/gateway/TenantDatabaseGateway.java
src/main/java/com/kompu/api/infrastructure/member/gateway/MemberDatabaseGateway.java
src/main/java/com/kompu/api/infrastructure/featureflag/gateway/FeatureFlagDatabaseGateway.java
src/main/java/com/kompu/api/infrastructure/tenantdomain/gateway/TenantDomainDatabaseGateway.java
```

### 3. Implement Helper Methods

- [ ] Replace `getOrCreateTenant()` with full implementation
- [ ] Implement `assignUserRole()` method
- [ ] Implement `createMemberRecord()` method
- [ ] Implement `initializeTenantFeatureFlags()` method
- [ ] Implement `setupTenantDomain()` method

### 4. Wire Dependencies in MvcConfiguration

```java
@Bean
public CreateTenantUseCase createTenantUseCase(...) { ... }

@Bean
public AssignUserRoleUseCase assignUserRoleUseCase(...) { ... }

@Bean
public CreateMemberUseCase createMemberUseCase(...) { ... }

@Bean
public InitializeFeatureFlagsUseCase initializeFeatureFlagsUseCase(...) { ... }

@Bean
public SetupTenantDomainUseCase setupTenantDomainUseCase(...) { ... }
```

### 5. Add Database Repositories

- [ ] `TenantRepository` extends `JpaRepository<TenantSchema, UUID>`
- [ ] `MemberRepository` extends `JpaRepository<MemberSchema, UUID>`
- [ ] `FeatureFlagRepository` extends `JpaRepository<FeatureFlagSchema, UUID>`
- [ ] `TenantDomainRepository` extends `JpaRepository<TenantDomainSchema, UUID>`

### 6. Create Schema Classes

- [ ] `TenantSchema` with JPA mapping
- [ ] `MemberSchema` with JPA mapping
- [ ] `FeatureFlagSchema` with JPA mapping
- [ ] `TenantDomainSchema` with JPA mapping

### 7. Add Exception Classes

- [ ] `TenantNotFoundException`
- [ ] `MemberCodeGenerationException`
- [ ] `FeatureFlagInitializationException`
- [ ] `DomainSetupException`

---

## Testing Guide

### Unit Tests

#### Test 1: Signup with Password Mismatch

```java
@Test
void signUp_passwordMismatch_returnsBadRequest() {
    ISignUpRequest request = new SignUpRequest(
        "johndoe",
        "john@example.com",
        "Password123!",
        "DifferentPass456!",  // Mismatch!
        "John Doe"
    );

    ResponseEntity<WebHttpResponse<IAuthTokenResponse>> response =
        authController.signUp(request, mockHttpRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
}
```

#### Test 2: Successful Signup with New Tenant

```java
@Test
void signUp_newTenant_createsUserAndReturnsToken() {
    ISignUpRequest request = new SignUpRequest(
        "janedoe",
        "jane@example.com",
        "SecurePassword123!",
        "SecurePassword123!",
        "Jane Doe"
    );

    ResponseEntity<WebHttpResponse<IAuthTokenResponse>> response =
        authController.signUp(request, mockHttpRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().getData()).isNotNull();
    assertThat(response.getBody().getData().accessToken()).isNotBlank();
    assertThat(response.getBody().getData().refreshToken()).isNotBlank();
}
```

#### Test 3: Verify User Has Admin Role

```java
@Test
void signUp_newTenant_assignsAdminRole() {
    // Create user
    UserAccountModel user = createUserUseCase.createUser(...);

    // Verify role assignment
    List<UserRoleModel> roles = userRoleGateway.findByUserId(user.getId());

    assertThat(roles).hasSize(1);
    assertThat(roles.get(0).getRole().getName()).isEqualTo("Admin");
}
```

### Integration Tests

#### Test 4: End-to-End Signup Flow

```java
@Test
void signUp_endToEnd_createsCompleteUserSetup() {
    // 1. Submit signup request
    ISignUpRequest request = new SignUpRequest(
        "newtenant",
        "admin@newtenant.com",
        "AdminPass123!",
        "AdminPass123!",
        "New Tenant Admin"
    );

    ResponseEntity<WebHttpResponse<IAuthTokenResponse>> response =
        authController.signUp(request, httpRequest);

    // 2. Verify user created
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    IAuthTokenResponse authResponse = response.getBody().getData();
    UUID userId = UUID.fromString(authResponse.user().id());

    // 3. Verify user has tenant
    UserAccountModel user = getUserUseCase.findById(userId);
    assertThat(user.getTenantId()).isNotNull();

    // 4. Verify role assigned
    List<UserRoleModel> roles = userRoleGateway.findByUserId(userId);
    assertThat(roles).hasSize(1);
    assertThat(roles.get(0).getRole().getName()).isEqualTo("Admin");

    // 5. Verify member record created
    MemberModel member = memberGateway.findByUserId(userId).orElseThrow();
    assertThat(member.getTenantId()).isEqualTo(user.getTenantId());
    assertThat(member.getStatus()).isEqualTo("active");

    // 6. Verify feature flags initialized
    List<FeatureFlagModel> flags = featureFlagGateway.findByTenantId(user.getTenantId());
    assertThat(flags).isNotEmpty();

    // 7. Verify domain created
    TenantDomainModel domain = tenantDomainGateway
        .findByTenantIdAndIsPrimary(user.getTenantId(), true).orElseThrow();
    assertThat(domain.getHost()).endsWith(".kompu.id");

    // 8. Verify tokens are valid
    String accessToken = authResponse.accessToken();
    String refreshToken = authResponse.refreshToken();
    assertThat(accessToken).isNotBlank();
    assertThat(refreshToken).isNotBlank();

    // 9. Verify session created
    UserSessionModel session = userSessionGateway.findById(...).orElseThrow();
    assertThat(session.getUserId()).isEqualTo(userId);
    assertThat(session.getTenantId()).isEqualTo(user.getTenantId());
}
```

#### Test 5: Verify RLS Isolation

```java
@Test
void signUp_multiTenant_dataIsolated() {
    // 1. Create user in Tenant 1
    UserAccountModel user1 = createUserInTenant(tenantId1, "user1");

    // 2. Create user in Tenant 2
    UserAccountModel user2 = createUserInTenant(tenantId2, "user2");

    // 3. Query users as Tenant1
    setSessionContext(tenantId1, user1.getId());
    List<UserAccountModel> tenant1Users = userGateway.findByTenantId(tenantId1);

    assertThat(tenant1Users).containsOnly(user1);
    assertThat(tenant1Users).doesNotContain(user2);

    // 4. Query users as Tenant2
    setSessionContext(tenantId2, user2.getId());
    List<UserAccountModel> tenant2Users = userGateway.findByTenantId(tenantId2);

    assertThat(tenant2Users).containsOnly(user2);
    assertThat(tenant2Users).doesNotContain(user1);
}
```

---

## Summary

The refactored `/signup` endpoint provides:

✅ **Multi-tenant support** - Users can create orgs or join existing tenants  
✅ **Role-based access** - Admin, Manager, Staff, Member roles auto-assigned  
✅ **Permission management** - Permissions derived from role assignments  
✅ **Member profiles** - Tenant-specific user data with member codes  
✅ **Feature toggles** - Per-tenant feature flag initialization  
✅ **Domain mapping** - Primary domain configuration with SSL/TLS  
✅ **Session tracking** - User sessions with IP and user agent logging  
✅ **JWT tokens** - Access tokens (1 hour) and refresh tokens (7 days)  
✅ **Data isolation** - RLS policies enforce tenant boundaries  
✅ **Clean architecture** - Use cases coordinate business logic

**Implementation Status:** Architecture complete, implementation in progress.
