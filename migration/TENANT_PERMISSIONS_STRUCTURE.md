# Tenant-Specific Permissions Structure

## Overview

The seeder script (`seeder_07122025.sql`) now implements a comprehensive multi-tenant permissions system with clear separation between global (super-admin) and tenant-scoped permissions.

## Permission Categories

### 1. Global Permissions (40+ permissions)

These permissions are system-wide and NOT scoped to individual tenants:

#### User Management

- `users.create` - Create new user account
- `users.read` - View user account details
- `users.update` - Edit user account information
- `users.delete` - Delete/deactivate user account

#### Role Management

- `roles.create` - Create new role
- `roles.read` - View roles
- `roles.update` - Edit role permissions
- `roles.delete` - Delete role

#### Product Management

- `products.create` - Create new product
- `products.read` - View products and catalog
- `products.update` - Edit product information and pricing
- `products.delete` - Delete product

#### Inventory Management

- `inventory.read` - View inventory levels
- `inventory.update` - Adjust inventory quantities

#### Order Management

- `orders.create` - Create new order
- `orders.read` - View orders
- `orders.update` - Edit order status and details
- `orders.delete` - Cancel/delete order

#### Payment Management

- `payments.read` - View payment records
- `payments.create` - Process payments
- `payments.update` - Update payment status

#### Member Management

- `members.create` - Register new member
- `members.read` - View member information
- `members.update` - Edit member details
- `members.delete` - Remove member

#### Financial Services

- `savings.read` - View savings accounts
- `savings.create` - Open savings account
- `savings.update` - Record savings transactions
- `loans.read` - View loan records
- `loans.create` - Create new loan
- `loans.update` - Update loan status and payments

#### System/Admin

- `system.config` - Manage system configuration
- `system.reports` - Generate reports
- `system.audit` - View audit logs and activity

#### Tenant Management (Super Admin Only)

- `tenants.create` - Create new tenant organization
- `tenants.read` - View tenant information
- `tenants.update` - Edit tenant settings and metadata
- `tenants.delete` - Delete/archive tenant

#### Domain Management (Super Admin Only)

- `domains.create` - Create tenant domain mappings
- `domains.read` - View domain configuration
- `domains.update` - Edit domain settings
- `domains.delete` - Remove domain mapping

### 2. Tenant-Specific Permissions (20+ permissions)

These permissions are designed for tenant-level operations and are assigned to tenant-scoped roles:

#### Dashboard & Analytics

- `dashboard.view` - Access tenant dashboard and analytics
- `analytics.view` - View detailed analytics and statistics
- `reports.export` - Export reports to CSV/PDF

#### Tenant Settings

- `settings.general` - Manage general tenant settings
- `settings.security` - Manage security and authentication settings
- `settings.notifications` - Configure notification preferences
- `settings.integration` - Manage third-party integrations

#### Bulk Operations

- `bulk.import` - Import bulk data (members, products)
- `bulk.export` - Export data in bulk
- `bulk.delete` - Delete records in bulk

#### Account Management

- `account.profile` - Manage own user profile and settings
- `account.password` - Change password
- `account.sessions` - Manage active sessions/devices

#### Approval/Workflow

- `approvals.view` - View pending approvals and requests
- `approvals.approve` - Approve requests (loans, registrations)
- `approvals.reject` - Reject/deny requests

#### Activity & Auditing

- `activity.log` - View activity logs
- `activity.export` - Export activity logs

#### Financial Reports

- `reports.financial` - Generate financial reports
- `reports.member` - Generate member reports
- `reports.sales` - Generate sales/order reports

## Global Roles (tenant_id = NULL)

### Super Admin

- **Description:** System administrator with full access to all tenants and configurations
- **Permissions:** ALL 60+ permissions (unrestricted)
- **Tenant Scope:** Global (NULL)
- **System Role:** Yes (is_system = true)
- **Use Case:** Platform administrators, super-user operations

### System

- **Description:** System internal account for background jobs and integrations
- **Permissions:** Limited to read operations and audit functions
  - `users.read`, `members.read`, `orders.read`, `payments.read`
  - `system.audit`, `activity.log`, `dashboard.view`
- **Tenant Scope:** Global (NULL)
- **System Role:** Yes (is_system = true)
- **Use Case:** Background jobs, scheduled tasks, API integrations

## Tenant-Scoped Roles

Each tenant has 4 roles with graduated permission levels:

### 1. Admin Role

**Tenant ID Examples:**

- Tenant 1: `30000000-0000-0000-0000-000000000001`
- Tenant 2: `30000000-0000-0000-0000-000000000101`

**Description:** Tenant administrator with full operational control

**Permissions:**

- All CRUD operations on core resources:
  - Users (create, read, update, delete)
  - Roles (read, create, update)
  - Products (create, read, update, delete)
  - Inventory (read, update)
  - Orders (create, read, update, delete)
  - Payments (read, create, update)
  - Members (create, read, update, delete)
  - Savings (read, create, update)
  - Loans (read, create, update)
- Administrative functions:
  - Dashboard and analytics access
  - Settings management (general, security, notifications)
  - Bulk import/export operations
  - Activity logs and exports
  - Approval operations (view, approve, reject)
  - Report generation (financial, member, sales)
  - Session management

**Excludes:**

- Super-admin functions (tenants._, domains._)
- System configuration

### 2. Manager Role

**Tenant ID Examples:**

- Tenant 1: `30000000-0000-0000-0000-000000000002`
- Tenant 2: `30000000-0000-0000-0000-000000000102`

**Description:** Department manager with supervisory permissions

**Permissions:**

- Read and update operations:
  - Products (read, update)
  - Inventory (read, update)
  - Orders (read, update)
  - Members (read, update)
  - Savings (read, update)
  - Loans (read, update)
- Reporting and analytics:
  - Dashboard view
  - Analytics view
  - Report export
  - Report generation (financial, member, sales)
  - Activity logs
- Approval operations:
  - View approvals
  - Approve requests
- Personal account management:
  - Profile and password management

### 3. Staff Role

**Tenant ID Examples:**

- Tenant 1: `30000000-0000-0000-0000-000000000003`
- Tenant 2: `30000000-0000-0000-0000-000000000103`

**Description:** Regular staff member with operational access

**Permissions:**

- Read-only access to products and members
- Operational transaction permissions:
  - Orders (read, create, update)
  - Payments (read, create)
  - Members (read, create)
- View-only analytics:
  - Dashboard view
  - Report export
  - Activity logs
- Personal account management:
  - Profile and password management

### 4. Member Role

**Tenant ID Examples:**

- Tenant 1: `30000000-0000-0000-0000-000000000004`
- Tenant 2: `30000000-0000-0000-0000-000000000104`

**Description:** Koperasi member with self-service access

**Permissions:**

- Limited read access:
  - Products (read)
  - Members (read)
  - Savings (read)
  - Loans (read)
- Self-service operations:
  - Orders (read, create)
- Analytics access:
  - Dashboard view
- Personal account management:
  - Profile and password management

## Feature Flags by Tenant

The seeder includes tenant-specific feature flags controlling feature availability:

### Global Flags

- `enable_two_factor_auth` - ENABLED (true)
- `enable_social_login` - DISABLED (false)
- `enable_api_access` - ENABLED (true)
- `maintenance_mode` - DISABLED (false)

### Tenant 1 (Koperasi Maju Sejahtera)

- `enable_inventory_tracking` - ENABLED
- `enable_loan_feature` - ENABLED
- `enable_savings_feature` - ENABLED
- `enable_monthly_reports` - ENABLED
- `enable_bulk_import` - ENABLED

### Tenant 2 (Koperasi Peduli Bersama)

- `enable_inventory_tracking` - ENABLED
- `enable_loan_feature` - ENABLED
- `enable_savings_feature` - ENABLED
- `enable_monthly_reports` - ENABLED
- `enable_bulk_import` - DISABLED

### Tenant 3 (Koperasi Nusantara Jaya)

- `enable_inventory_tracking` - ENABLED
- `enable_loan_feature` - DISABLED
- `enable_savings_feature` - ENABLED
- `enable_monthly_reports` - DISABLED

## Permission Architecture Highlights

### 1. **Clear Separation of Concerns**

- Global permissions for system-level operations
- Tenant permissions for business-specific features
- Super Admin has all permissions
- System role has minimal read-only permissions

### 2. **Graduated Access Control**

- **Admin:** Full operational control within tenant
- **Manager:** Supervisory and reporting access
- **Staff:** Transactional and operational access
- **Member:** Self-service access only

### 3. **RLS Compatibility**

All permissions work in conjunction with PostgreSQL Row-Level Security (RLS) policies defined in `initial_07122025.sql`. The RLS policies ensure:

- Users cannot access data from other tenants
- Global roles (tenant_id = NULL) have special handling
- Super Admin bypass capability

### 4. **Extensibility**

The permission system is designed to allow:

- Adding new permissions without schema changes
- Creating custom roles by combining permissions
- Per-tenant feature flag overrides
- Dynamic permission assignment to new roles

## Sample Permission Assignment Query

To verify tenant-scoped permissions:

```sql
SELECT
  r.id,
  r.name,
  r.tenant_id,
  t.name as tenant_name,
  COUNT(rp.permission_id) as permission_count
FROM app.roles r
LEFT JOIN app.tenants t ON r.tenant_id = t.id
LEFT JOIN app.role_permissions rp ON r.id = rp.role_id
WHERE r.tenant_id IS NOT NULL
GROUP BY r.id, r.name, r.tenant_id, t.name
ORDER BY r.tenant_id, r.name;
```

## Usage in Spring Boot Application

The Spring Boot application uses these permissions in:

1. **Method-Level Security** - Via `@PreAuthorize` annotations
2. **Endpoint Authorization** - In `AppSecurityConfigurer`
3. **Custom Authorization** - Via `PermissionChecker` service
4. **Audit Logging** - Tracking permission usage in `auth_audit` table

Example:

```java
@PreAuthorize("hasPermission('products:create')")
public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
    // Product creation logic
}
```

## Database Seed Statistics

After running `seeder_07122025.sql`:

- **Global Permissions:** 44
- **Tenant-Specific Permissions:** 20
- **Total Permissions:** 64
- **Global Roles:** 2 (Super Admin, System)
- **Tenant Roles per Tenant:** 4 (Admin, Manager, Staff, Member)
- **Total Tenant Roles:** 8 (2 tenants × 4 roles)
- **Total Roles:** 10
- **Role-Permission Assignments:** 150+
- **Feature Flags:** 18 (4 global + 14 tenant-specific)

## Migration Path

To add new permissions:

1. Insert into `app.permissions` table
2. Reference in seeder or via SQL migration
3. Assign to existing roles via `app.role_permissions`
4. Update Spring Boot authorization annotations
5. Deploy and run seeder to populate new permissions

## Best Practices

1. **Always assign permissions to roles**, not directly to users
2. **Use feature flags** for gradual feature rollout
3. **Audit permission changes** via activity logs
4. **Document new permissions** in this file
5. **Test permission enforcement** with multiple user roles
6. **Review tenant isolation** periodically via RLS policies
