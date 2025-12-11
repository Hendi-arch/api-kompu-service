# Tenant-Specific Permissions Implementation Summary

## What's Been Added

### 1. **Enhanced Seeder Script** (`seeder_07122025.sql`)

The seeder has been significantly expanded to include:

#### Global Permissions (44 permissions)

- **User Management** (4): CRUD operations on user accounts
- **Role Management** (4): Role creation and permission assignment
- **Product Management** (4): Product catalog operations
- **Inventory Management** (2): Stock level tracking
- **Order Management** (4): Order lifecycle operations
- **Payment Management** (3): Payment processing
- **Member Management** (4): Member registration and management
- **Savings** (3): Savings account operations
- **Loans** (3): Loan management
- **System Admin** (3): Configuration and auditing
- **Tenant Management** (4): Super-admin only - create/manage tenants
- **Domain Management** (4): Super-admin only - configure domains

#### Tenant-Specific Permissions (20 permissions)

- **Dashboard & Analytics** (3): Dashboard access, analytics, report export
- **Tenant Settings** (4): General, security, notifications, integrations
- **Bulk Operations** (3): Import, export, bulk delete
- **Account Management** (3): Profile, password, sessions
- **Approvals & Workflow** (3): View, approve, reject operations
- **Activity & Auditing** (2): Activity logs, exports
- **Financial Reports** (3): Financial, member, and sales reports

#### Global Roles (2)

1. **Super Admin** (Global, tenant_id = NULL)

   - Full access to all 64 permissions
   - System management capabilities
   - Multi-tenant operations

2. **System** (Global, tenant_id = NULL)
   - Limited read-only access (7 permissions)
   - For background jobs and integrations
   - Secure operational access

#### Tenant-Scoped Roles (4 per tenant × 3 tenants = 12 roles)

Each tenant has 4 graduated roles:

1. **Admin** (Tenant-specific)

   - 48 permissions (75% of available)
   - Full operational control
   - Settings, approvals, reporting
   - Cannot manage tenants/domains

2. **Manager** (Tenant-specific)

   - 26 permissions (41% of available)
   - Update and read permissions
   - Analytics and reporting
   - Approval authority
   - Supervisory functions

3. **Staff** (Tenant-specific)

   - 16 permissions (25% of available)
   - Read-only on most resources
   - Create/update orders and payments
   - Dashboard and basic reporting

4. **Member** (Tenant-specific)
   - 11 permissions (17% of available)
   - Self-service access
   - Create orders, view own data
   - Profile and password management

#### Sample Tenants (3)

1. **Koperasi Maju Sejahtera** (Jakarta)

   - UUID: `20000000-0000-0000-0000-000000000001`
   - Status: Active
   - 150 members

2. **Koperasi Peduli Bersama** (Surabaya)

   - UUID: `20000000-0000-0000-0000-000000000002`
   - Status: Active
   - 200 members

3. **Koperasi Nusantara Jaya** (Bandung)
   - UUID: `20000000-0000-0000-0000-000000000003`
   - Status: Active
   - 120 members

#### Feature Flags (18 total)

- **4 Global flags** - System-wide feature toggles
- **14 Tenant-specific flags** - Per-tenant feature control
  - Inventory tracking
  - Loan features
  - Savings features
  - Monthly reporting
  - Bulk import capabilities

#### Tenant Domains

- Primary and custom domains per tenant
- SSL/TLS provider configuration
- Domain-to-tenant routing support

### 2. **Documentation Files**

#### `TENANT_PERMISSIONS_STRUCTURE.md`

Comprehensive guide covering:

- All permission categories and descriptions
- Global role definitions and use cases
- Tenant-scoped role hierarchy
- Feature flags by tenant
- Permission architecture highlights
- RLS compatibility notes
- Spring Boot integration examples
- Database seed statistics
- Migration path for new permissions

#### `TENANT_PERMISSIONS_MATRIX.md`

Quick-reference tables showing:

- 64×6 permission coverage matrix
- Permission summary statistics
- Permission counts by category
- Common permission sets
- Permission scope matrix (global vs tenant)
- SQL query examples for verification
- Permission inheritance patterns

### 3. **Key Design Decisions**

#### Separation of Concerns

✅ Global permissions for platform-level operations
✅ Tenant permissions for business-specific features
✅ Clear super-admin vs system role distinction

#### Graduated Access Control

✅ Admin (75%) > Manager (41%) > Staff (25%) > Member (17%)
✅ Each level has specific, non-overlapping permission sets
✅ Clear use case for each role

#### Multi-Tenant Isolation

✅ Tenant_id = NULL for global operations
✅ Tenant_id required for tenant-scoped operations
✅ RLS policies enforce isolation

#### Extensibility

✅ New permissions can be added without schema changes
✅ Custom roles via permission combination
✅ Feature flags for gradual rollout

## Implementation Details

### Permission Count Summary

| Category                    | Total  |
| --------------------------- | :----: |
| Global Permissions          |   44   |
| Tenant-Specific Permissions |   20   |
| **Total Permissions**       | **64** |

### Role Count Summary

| Type                     | Count | Total  |
| ------------------------ | :---: | :----: |
| Global Roles             |   2   |   2    |
| Tenant Roles per Tenant  |   4   |   4    |
| Tenant Roles (3 tenants) | 4 × 3 | **12** |
| **Total Roles**          |   -   | **14** |

### Permission Assignment Summary

- Super Admin: 64/64 permissions (100%)
- System: 7/64 permissions (11%)
- Tenant Admin: 48/64 permissions (75%)
- Tenant Manager: 26/64 permissions (41%)
- Tenant Staff: 16/64 permissions (25%)
- Tenant Member: 11/64 permissions (17%)
- **Total assignments**: 150+ role-permission pairs

## How It Works

### 1. User Authentication

User logs in → JWT token generated → Token includes user_id and tenant_id

### 2. Permission Resolution

```
User → User Roles → Role Permissions → Permission Check
```

### 3. Authorization Flow

```
@PreAuthorize("hasPermission('products:create')")
  ↓
SecurityContext retrieves permissions from JWT
  ↓
Compare requested permission against user's permissions
  ↓
If found in user's permission set → Allow
Else → Deny (403 Forbidden)
```

### 4. Tenant Isolation

```
RLS Policy checks:
- Is permission tenant-scoped?
  - If YES: Check tenant_id matches session tenant
  - If NO: Only super-admin can execute
```

## Migration to Production

### Step 1: Run Schema Migration

```bash
psql -U postgres -d kompu_prod -f migration/initial_07122025.sql
```

### Step 2: Run Seeder

```bash
psql -U postgres -d kompu_prod -f migration/seeder_07122025.sql
```

### Step 3: Verify Data

```sql
-- Check permissions
SELECT COUNT(*) FROM app.permissions;  -- Should be 64

-- Check roles
SELECT COUNT(*) FROM app.roles;  -- Should be 14

-- Check role-permission mappings
SELECT COUNT(*) FROM app.role_permissions;  -- Should be 150+

-- Check feature flags
SELECT COUNT(*) FROM app.feature_flags;  -- Should be 18
```

### Step 4: Configure Spring Boot

```properties
# application.properties
jwt.secret=your-secret-key
jwt.expiration=3600
spring.jpa.properties.spring.jpa.hibernate.enable_lazy_load_no_trans=true
```

### Step 5: Deploy and Test

- Test with super-admin user
- Test with tenant-specific users
- Verify permission enforcement
- Check RLS policy enforcement

## Testing Scenarios

### Scenario 1: Super Admin Operations

- ✅ Create tenant
- ✅ Configure domains
- ✅ Access all data across all tenants
- ✅ Manage global configuration

### Scenario 2: Tenant Admin Operations

- ✅ Create products for their tenant
- ✅ Manage users within their tenant
- ✅ Cannot access other tenant's data (RLS prevents)
- ✅ Cannot create new tenants

### Scenario 3: Manager Operations

- ✅ View analytics and reports
- ✅ Approve loan requests
- ✅ Cannot delete members or products
- ✅ Cannot manage settings

### Scenario 4: Staff Operations

- ✅ Create orders
- ✅ Process payments
- ✅ Cannot modify products or pricing
- ✅ Cannot approve operations

### Scenario 5: Member Operations

- ✅ Create own orders
- ✅ View own data
- ✅ Cannot access other members' data (RLS prevents)
- ✅ Limited to self-service

## Maintenance & Updates

### Adding a New Permission

```sql
INSERT INTO app.permissions (id, code, description, created_at)
VALUES (gen_random_uuid(), 'new.permission', 'Description', now());
```

### Adding a New Role

```sql
INSERT INTO app.roles (id, tenant_id, name, description, is_system, created_at, updated_at)
VALUES (gen_random_uuid(), '<tenant-uuid>', 'Role Name', 'Description', false, now(), now());
```

### Assigning Permissions to Role

```sql
INSERT INTO app.role_permissions (role_id, permission_id)
SELECT '<role-uuid>', id FROM app.permissions WHERE code IN ('perm1', 'perm2');
```

### Revoking Permissions

```sql
DELETE FROM app.role_permissions
WHERE role_id = '<role-uuid>' AND permission_id IN (SELECT id FROM app.permissions WHERE code = 'permission');
```

## Spring Boot Integration

The permissions are intended to be used with:

1. **Method Security**

```java
@PreAuthorize("hasPermission('products:create')")
public ResponseEntity<ProductResponse> createProduct(...) { }
```

2. **Route Security**

```java
.antMatchers("/api/v1/products/**").hasPermission("products:read")
.antMatchers("/api/v1/admin/**").hasPermission("system:config")
```

3. **Custom Authorization Service**

```java
public class PermissionChecker {
    public boolean hasPermission(String permission) {
        return userPermissions.contains(permission);
    }
}
```

## Troubleshooting

### Q: User cannot access data from their tenant

A: Check RLS policies and ensure `app.current_tenant` is set correctly in session config

### Q: Permission not being enforced

A: Verify permission code matches exactly in `@PreAuthorize` annotation

### Q: Getting 403 Forbidden on allowed endpoint

A: Check user-role assignment and role-permission assignment in database

### Q: Feature flag not working

A: Verify feature flag key is exactly matching in code, check `enabled` column

## Support & Documentation

For additional help:

- See `TENANT_PERMISSIONS_STRUCTURE.md` for detailed structure
- See `TENANT_PERMISSIONS_MATRIX.md` for quick reference
- Review `initial_07122025.sql` for schema details
- Check Spring Boot security configuration in source code
