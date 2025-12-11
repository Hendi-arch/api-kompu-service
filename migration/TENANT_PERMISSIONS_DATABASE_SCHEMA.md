# Tenant-Specific Permissions Database Schema

## Entity Relationship Diagram (ERD)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         PERMISSIONS SYSTEM                              │
└─────────────────────────────────────────────────────────────────────────┘

    ┌──────────────────┐
    │   TENANTS        │
    ├──────────────────┤
    │ id (PK)          │
    │ name             │
    │ code (UNIQUE)    │
    │ status           │
    │ metadata (JSON)  │
    │ created_at       │
    │ created_by       │
    └────────┬─────────┘
             │
             │ (1:N)
             │
    ┌────────▼──────────────┐
    │ ROLES (Tenant-scoped) │
    ├──────────────────────┤
    │ id (PK)              │
    │ tenant_id (FK)       │ ◄─────── NULL = Global Role
    │ name                 │
    │ description          │
    │ is_system            │
    │ created_at           │
    └────────┬─────────────┘
             │
             │ (M:N via ROLE_PERMISSIONS)
             │
    ┌────────▼──────────────────┐
    │  ROLE_PERMISSIONS         │
    ├──────────────────────────┤
    │ role_id (FK, PK)          │
    │ permission_id (FK, PK)    │
    └────────▲──────────────────┘
             │
             │ (N:1)
             │
    ┌────────┴──────────────┐
    │  PERMISSIONS          │
    ├───────────────────────┤
    │ id (PK)               │
    │ code (UNIQUE)         │
    │ description           │
    │ created_at            │
    └───────────────────────┘

    ┌──────────────────┐
    │   USERS          │
    ├──────────────────┤
    │ id (PK)          │
    │ tenant_id (FK)   │ ◄─────── NULL = Global User
    │ username         │
    │ email            │
    │ password_hash    │
    │ is_active        │
    │ created_by       │
    └────────┬─────────┘
             │
             │ (1:N via USER_ROLES)
             │
    ┌────────▼──────────────┐
    │  USER_ROLES           │
    ├───────────────────────┤
    │ user_id (FK, PK)      │
    │ role_id (FK, PK)      │
    │ assigned_at           │
    └───────────────────────┘
             │
             │ references
             ▼
         ROLES (M:N)
```

## Table Schemas

### 1. PERMISSIONS Table

```sql
CREATE TABLE app.permissions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code text NOT NULL UNIQUE,
    description text,
    created_at timestamptz NOT NULL DEFAULT now()
);

-- Indexes:
-- UNIQUE INDEX on code (for fast lookups)
-- Used in: @PreAuthorize, hasPermission() checks
```

**Sample Data:**

```
| id | code | description | created_at |
|---|---|---|---|
| uuid-1 | users.create | Create new user account | 2025-01-01 |
| uuid-2 | products.read | View products and catalog | 2025-01-01 |
| uuid-3 | dashboard.view | Access tenant dashboard | 2025-01-01 |
```

### 2. ROLES Table

```sql
CREATE TABLE app.roles (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id uuid,  -- NULL = Global Role, <uuid> = Tenant-scoped
    name text NOT NULL,
    description text,
    is_system boolean DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

-- UNIQUE INDEX on (tenant_id, name) ensures unique role names per tenant
-- is_system = true for Super Admin and System roles (tenant_id = NULL)
```

**Sample Data:**

```
| id | tenant_id | name | description | is_system |
|---|---|---|---|---|
| uuid-10 | NULL | Super Admin | Full system access | true |
| uuid-11 | NULL | System | Background jobs | true |
| uuid-30 | tenant-1 | Admin | Tenant operator | false |
| uuid-31 | tenant-1 | Manager | Supervisory access | false |
| uuid-32 | tenant-2 | Admin | Tenant operator | false |
```

### 3. ROLE_PERMISSIONS Table (Junction Table)

```sql
CREATE TABLE app.role_permissions (
    role_id uuid NOT NULL REFERENCES app.roles(id) ON DELETE CASCADE,
    permission_id uuid NOT NULL REFERENCES app.permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- M:N relationship allowing flexible permission assignment
-- Cascade delete ensures cleanup when roles/permissions are removed
```

**Sample Data:**

```
| role_id | permission_id |
|---|---|
| uuid-30 (Admin) | uuid-1 (users.create) |
| uuid-30 (Admin) | uuid-2 (products.read) |
| uuid-30 (Admin) | uuid-3 (dashboard.view) |
| uuid-31 (Manager) | uuid-2 (products.read) |
| uuid-31 (Manager) | uuid-3 (dashboard.view) |
```

### 4. USERS Table

```sql
CREATE TABLE app.users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id uuid,  -- NULL = Global/Super-admin user
    username text NOT NULL,
    email text NOT NULL,
    password_hash text,
    full_name text,
    is_active boolean DEFAULT true,
    is_email_verified boolean DEFAULT false,
    created_by uuid,
    updated_by uuid,
    deleted_at timestamptz
);

-- UNIQUE INDEX on (tenant_id, username)
-- UNIQUE INDEX on (tenant_id, email)
-- Soft-delete via deleted_at
```

**Sample Data:**

```
| id | tenant_id | username | email | is_active |
|---|---|---|---|---|
| uuid-100 | NULL | superadmin | admin@kompu.id | true |
| uuid-101 | tenant-1 | admin.komaju | admin@komaju.id | true |
| uuid-102 | tenant-1 | manager.komaju | mgr@komaju.id | true |
| uuid-103 | tenant-2 | admin.kopeduli | admin@kopeduli.id | true |
```

### 5. USER_ROLES Table (Junction Table)

```sql
CREATE TABLE app.user_roles (
    user_id uuid NOT NULL REFERENCES app.users(id) ON DELETE CASCADE,
    role_id uuid NOT NULL REFERENCES app.roles(id) ON DELETE CASCADE,
    assigned_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);

-- M:N relationship between users and roles
-- Single user can have multiple roles
-- Single role can be assigned to multiple users
```

**Sample Data:**

```
| user_id | role_id | assigned_at |
|---|---|---|
| uuid-100 | uuid-10 (Super Admin) | 2025-01-01 |
| uuid-101 | uuid-30 (Tenant 1 Admin) | 2025-01-01 |
| uuid-102 | uuid-31 (Tenant 1 Manager) | 2025-01-01 |
| uuid-103 | uuid-32 (Tenant 2 Admin) | 2025-01-01 |
```

### 6. TENANTS Table

```sql
CREATE TABLE app.tenants (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name text NOT NULL,
    code text UNIQUE,
    status text NOT NULL DEFAULT 'active',  -- active|suspended|archived
    metadata jsonb DEFAULT '{}'::jsonb,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by uuid,
    updated_by uuid,
    deleted_at timestamptz
);

-- CODE is unique across all tenants
-- STATUS is used for soft disabling without deletion
-- METADATA stores flexible tenant configuration
```

**Sample Data:**

```
| id | name | code | status |
|---|---|---|---|
| tenant-1 | Koperasi Maju Sejahtera | KOMAJU | active |
| tenant-2 | Koperasi Peduli Bersama | KOPEDULI | active |
| tenant-3 | Koperasi Nusantara Jaya | KONUS | active |
```

### 7. FEATURE_FLAGS Table

```sql
CREATE TABLE app.feature_flags (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id uuid,  -- NULL = Global flag
    key text NOT NULL,
    value jsonb NOT NULL,
    enabled boolean DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

-- UNIQUE INDEX on (tenant_id, key) prevents duplicate flags per tenant
-- ENABLED column allows quick feature toggle
-- VALUE can store complex feature configuration
```

**Sample Data:**

```
| id | tenant_id | key | value | enabled |
|---|---|---|---|---|
| uuid-flag-1 | NULL | enable_api_access | "true"::jsonb | true |
| uuid-flag-2 | tenant-1 | enable_loan_feature | "true"::jsonb | true |
| uuid-flag-3 | tenant-2 | enable_loan_feature | "true"::jsonb | true |
| uuid-flag-4 | tenant-3 | enable_loan_feature | "false"::jsonb | false |
```

## Permission Resolution Flow

### 1. User Authentication

```
POST /api/v1/auth/signin
  ↓
Validate credentials
  ↓
Query user roles: SELECT * FROM user_roles WHERE user_id = $1
  ↓
Query role permissions:
  SELECT DISTINCT p.* FROM permissions p
  JOIN role_permissions rp ON p.id = rp.permission_id
  WHERE rp.role_id IN (user's role IDs)
  ↓
Build JWT token with permissions list
  ↓
Return token + refresh token
```

### 2. Permission Check on Request

```
GET /api/v1/products/new
  ↓
Extract JWT token from header
  ↓
Parse JWT → Extract user_id, tenant_id, permissions array
  ↓
@PreAuthorize("hasPermission('products:create')")
  ↓
Check if "products.create" in permissions array
  ↓
  ├─ YES → Execute endpoint logic
  └─ NO → Return 403 Forbidden
```

### 3. Multi-Tenant Isolation (RLS)

```
Query data from tenant-scoped table (e.g., products)
  ↓
RLS Policy evaluates:
  - Is tenant_id = NULL? (Global data)
    └─ Can super-admin access? → YES
  - Is tenant_id = current_tenant()? (User's tenant)
    └─ YES → Allow access
  - Else: Deny access
  ↓
Only matching rows returned
```

## Query Examples

### Find all permissions for a user

```sql
SELECT DISTINCT p.code, p.description
FROM app.users u
JOIN app.user_roles ur ON u.id = ur.user_id
JOIN app.roles r ON ur.role_id = r.id
JOIN app.role_permissions rp ON r.id = rp.role_id
JOIN app.permissions p ON rp.permission_id = p.id
WHERE u.id = $1::uuid
ORDER BY p.code;
```

### Find all users with a specific permission in a tenant

```sql
SELECT DISTINCT u.id, u.username, u.full_name
FROM app.users u
JOIN app.user_roles ur ON u.id = ur.user_id
JOIN app.roles r ON ur.role_id = r.id
JOIN app.role_permissions rp ON r.id = rp.role_id
JOIN app.permissions p ON rp.permission_id = p.id
WHERE u.tenant_id = $1::uuid
  AND p.code = $2::text
ORDER BY u.username;
```

### Get role hierarchy for a tenant

```sql
SELECT
  r.name,
  COUNT(rp.permission_id) as permission_count,
  STRING_AGG(p.code, ', ' ORDER BY p.code) as permissions
FROM app.roles r
LEFT JOIN app.role_permissions rp ON r.id = rp.role_id
LEFT JOIN app.permissions p ON rp.permission_id = p.id
WHERE r.tenant_id = $1::uuid
GROUP BY r.id, r.name
ORDER BY permission_count DESC;
```

### Check feature flag status for tenant

```sql
SELECT key, value, enabled
FROM app.feature_flags
WHERE (tenant_id = $1::uuid OR tenant_id IS NULL)
ORDER BY tenant_id DESC
LIMIT 1;
```

## Constraints & Rules

### 1. Permission Codes

- **Format:** `resource.action` (e.g., `products.create`)
- **Case:** lowercase with dots as separators
- **Uniqueness:** Global across all permissions
- **Immutable:** Code should not change once created

### 2. Role Names

- **Uniqueness:** Unique per tenant (tenant_id, name)
- **System Roles:** Only in global scope (tenant_id = NULL)
- **Case:** PascalCase or lowercase_with_underscores

### 3. User-Role Assignment

- **Multiplicity:** Users can have multiple roles
- **Scope:** User can only have roles from their tenant
- **Constraints:** Foreign key ensures role exists

### 4. Tenant Isolation

- **RLS Policies:** Enforce at database level
- **Session Context:** Must set app.current_tenant on connection
- **Cascade Behavior:** Deleting tenant cascades to all related data

## Performance Considerations

### Indexing Strategy

```sql
-- Permission lookups (common in every request)
CREATE INDEX idx_permissions_code ON app.permissions(code);

-- User permission resolution (authentication)
CREATE INDEX idx_user_roles_user_id ON app.user_roles(user_id);
CREATE INDEX idx_role_permissions_role_id ON app.role_permissions(role_id);

-- Role-permission joins (permission checks)
CREATE INDEX idx_role_permissions_perm_id ON app.role_permissions(permission_id);

-- Tenant scoped queries
CREATE UNIQUE INDEX idx_roles_tenant_name ON app.roles(tenant_id, lower(name));
CREATE INDEX idx_feature_flags_tenant_key ON app.feature_flags(tenant_id, lower(key));
```

### Query Optimization Tips

1. **Cache permission results** in JWT token during authentication
2. **Use batch permission checks** instead of individual queries
3. **Avoid N+1 queries** when loading user roles and permissions
4. **Preload permissions** for common operations

## Security Best Practices

1. **Never expose permission codes** in frontend (use high-level concepts)
2. **Always validate permissions** server-side, never trust client
3. **Log permission denied events** for security auditing
4. **Regularly audit role assignments** to detect privilege creep
5. **Use feature flags** for gradual permission rollout
6. **Test permission enforcement** in automated test suite
7. **Document permission meanings** clearly for maintainers
8. **Review sensitive permissions** quarterly (e.g., .delete, .admin)
