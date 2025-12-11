# Tenant Permissions Documentation Index

## Overview

This directory contains comprehensive documentation for the multi-tenant permissions system implemented in the `seeder_07122025.sql` database seeder script.

## Quick Navigation

### 📋 For Different Audiences

#### **Project Managers / Business Stakeholders**

→ Start here: [TENANT_PERMISSIONS_README.md](./TENANT_PERMISSIONS_README.md)

- What's being added
- Why it matters
- Key features overview
- Timeline and rollout plan

#### **Developers**

→ Start here: [TENANT_PERMISSIONS_STRUCTURE.md](./TENANT_PERMISSIONS_STRUCTURE.md)

- Complete permission catalog
- Role definitions with use cases
- Spring Boot integration patterns
- Implementation guidelines

#### **Database Administrators**

→ Start here: [TENANT_PERMISSIONS_DATABASE_SCHEMA.md](./TENANT_PERMISSIONS_DATABASE_SCHEMA.md)

- Table schemas and relationships
- ERD diagrams
- Query examples
- Performance optimization tips
- Security best practices

#### **Quick Reference / Cheat Sheet**

→ Use: [TENANT_PERMISSIONS_MATRIX.md](./TENANT_PERMISSIONS_MATRIX.md)

- Permission-role matrix table
- At-a-glance permission coverage
- Common permission sets
- Query templates

## Documentation Files

### 1. **TENANT_PERMISSIONS_README.md** (This file's companion)

**Purpose:** Executive summary and implementation guide

**Contents:**

- What's been added (64 permissions, 14 roles, 3 tenants)
- Key design decisions
- Implementation details
- How permissions work end-to-end
- Migration steps to production
- Testing scenarios

**Best for:** Overall understanding, project planning

---

### 2. **TENANT_PERMISSIONS_STRUCTURE.md**

**Purpose:** Comprehensive reference documentation

**Contents:**

- Permission categories (40+ global, 20+ tenant-specific)
- Role definitions with full permission lists:
  - Super Admin (global)
  - System (global)
  - Admin (tenant, 48 permissions)
  - Manager (tenant, 26 permissions)
  - Staff (tenant, 16 permissions)
  - Member (tenant, 11 permissions)
- Feature flags by tenant
- Architecture highlights
- RLS compatibility notes
- Spring Boot integration examples

**Best for:** Understanding permission architecture, implementing authorization

---

### 3. **TENANT_PERMISSIONS_MATRIX.md**

**Purpose:** Quick lookup reference with visual tables

**Contents:**

- 64×6 permission coverage matrix (permissions vs roles)
- Permission summary statistics
- Permission breakdown by category (19 categories)
- Common permission sets (e.g., "Create Product", "View Dashboard")
- Permission scope matrix (global vs tenant)
- SQL query examples for verification
- Practical examples

**Best for:** Quick lookups, role design, testing

---

### 4. **TENANT_PERMISSIONS_DATABASE_SCHEMA.md**

**Purpose:** Database structure and technical reference

**Contents:**

- Entity Relationship Diagram (ERD)
- Table schemas with descriptions:
  - permissions
  - roles
  - role_permissions
  - users
  - user_roles
  - tenants
  - feature_flags
- Permission resolution flow diagrams
- Detailed query examples
- Constraints and rules
- Performance considerations
- Security best practices

**Best for:** Database design understanding, SQL queries, optimization

---

### 5. **Initial Schema**: `initial_07122025.sql`

**Purpose:** Database schema creation (provided separately)

**Contains:**

- Table definitions
- Indexes
- RLS policies
- Audit functions
- Trigger setup

**Related to:** TENANT_PERMISSIONS_DATABASE_SCHEMA.md (for understanding structure)

---

### 6. **Seeder Script**: `seeder_07122025.sql`

**Purpose:** Sample data initialization

**Contains:**

- 64 permission definitions
- 14 role definitions (2 global + 12 tenant-scoped)
- 150+ role-permission assignments
- 3 sample tenants
- 8 tenant-scoped roles
- 18 feature flags
- Configuration settings

**Related to:** All documentation files

---

## Key Concepts

### Permissions (64 Total)

| Type            | Count | Examples                                      |
| --------------- | :---: | --------------------------------------------- |
| Global          |  44   | users.create, tenants.read, system.audit      |
| Tenant-specific |  20   | dashboard.view, settings.general, bulk.import |

### Roles (14 Total)

**Global Roles (2)**

- `Super Admin` - Full system access
- `System` - Background job access

**Tenant-Scoped Roles (12)**

- 4 roles × 3 sample tenants
- Admin, Manager, Staff, Member

### Tenants (3 Sample)

1. Koperasi Maju Sejahtera (Jakarta)
2. Koperasi Peduli Bersama (Surabaya)
3. Koperasi Nusantara Jaya (Bandung)

### Permission Flow

```
User Logs In
  ↓
Query: user → user_roles → roles → role_permissions → permissions
  ↓
Build JWT with permission list
  ↓
Request to Protected Endpoint
  ↓
@PreAuthorize checks permission
  ↓
RLS policy checks tenant isolation
  ↓
Execute or Deny (403)
```

## Permission Categories Reference

### Core Operations (32 permissions)

- User Management (4)
- Role Management (4)
- Product Management (4)
- Inventory (2)
- Order Management (4)
- Payment Management (3)
- Member Management (4)
- Savings (3)

### System Operations (10 permissions)

- System Admin (3)
- Tenant Management (4)
- Domain Management (4)

### Tenant Features (20 permissions)

- Dashboard & Analytics (3)
- Tenant Settings (4)
- Bulk Operations (3)
- Account Management (3)
- Approvals & Workflow (3)
- Activity & Auditing (2)
- Financial Reports (3)
- (1 category with 2 permissions)

## Role Permission Coverage

| Role           | Permissions | Coverage | Use Case               |
| -------------- | :---------: | :------: | ---------------------- |
| Super Admin    |     64      |   100%   | Platform administrator |
| System         |      7      |   11%    | Background jobs        |
| Tenant Admin   |     48      |   75%    | Tenant operator        |
| Tenant Manager |     26      |   41%    | Supervisory            |
| Tenant Staff   |     16      |   25%    | Transactional          |
| Tenant Member  |     11      |   17%    | Self-service           |

## Feature Flags

### Global (4)

- enable_two_factor_auth ✅
- enable_social_login ❌
- enable_api_access ✅
- maintenance_mode ❌

### Per-Tenant Examples

**Tenant 1:**

- Inventory tracking ✅
- Loans ✅
- Savings ✅
- Monthly reports ✅
- Bulk import ✅

**Tenant 2:**

- Inventory tracking ✅
- Loans ✅
- Savings ✅
- Monthly reports ✅
- Bulk import ❌

**Tenant 3:**

- Inventory tracking ✅
- Loans ❌
- Savings ✅
- Monthly reports ❌

## SQL Query Patterns

### Check user permissions

```sql
SELECT p.code FROM app.permissions p
JOIN app.role_permissions rp ON p.id = rp.permission_id
JOIN app.roles r ON rp.role_id = r.id
JOIN app.user_roles ur ON r.id = ur.role_id
WHERE ur.user_id = $1;
```

### Find users with permission

```sql
SELECT u.username FROM app.users u
JOIN app.user_roles ur ON u.id = ur.user_id
JOIN app.roles r ON ur.role_id = r.id
JOIN app.role_permissions rp ON r.id = rp.role_id
JOIN app.permissions p ON rp.permission_id = p.id
WHERE p.code = $1 AND u.tenant_id = $2;
```

### Get role permissions

```sql
SELECT p.code, p.description FROM app.permissions p
JOIN app.role_permissions rp ON p.id = rp.permission_id
WHERE rp.role_id = $1
ORDER BY p.code;
```

See [TENANT_PERMISSIONS_DATABASE_SCHEMA.md](./TENANT_PERMISSIONS_DATABASE_SCHEMA.md) for more examples.

## Implementation Steps

1. **Schema**: Run `initial_07122025.sql` ← Sets up tables & RLS
2. **Seeder**: Run `seeder_07122025.sql` ← Adds permissions & roles
3. **Spring Boot**: Configure security in `AppSecurityConfigurer`
4. **Endpoints**: Add `@PreAuthorize` annotations
5. **JWT**: Include permissions in token payload
6. **Test**: Verify with sample users and permissions

Detailed steps in [TENANT_PERMISSIONS_README.md](./TENANT_PERMISSIONS_README.md)

## Testing Checklist

- [ ] Create user with Admin role
- [ ] Create user with Manager role
- [ ] Create user with Staff role
- [ ] Create user with Member role
- [ ] Test @PreAuthorize on protected endpoints
- [ ] Test RLS isolation between tenants
- [ ] Test feature flag behavior
- [ ] Test permission denied (403) response
- [ ] Test JWT token includes permissions
- [ ] Test global vs tenant-scoped operations

## Related Files in Project

- **Spring Boot Security**: `infrastructure/config/web/AppSecurityConfigurer.java`
- **JWT Handler**: `infrastructure/config/web/MyAuthenticationHandler.java`
- **User Details Service**: `infrastructure/config/web/MyUserDetailService.java`
- **Authorization Controller**: `infrastructure/auth/controller/AuthController.java`
- **Permission Service** (custom): May need to be created

## Maintenance Tasks

### Regular (Monthly)

- [ ] Review role assignments for privilege creep
- [ ] Audit permission denied events
- [ ] Check feature flag effectiveness

### As Needed

- [ ] Add new permission (insert, assign to role)
- [ ] Create new role (insert, assign permissions)
- [ ] Modify role permissions (insert/delete associations)
- [ ] Update feature flags (modify feature_flags table)

### Quarterly Review

- [ ] Security audit of sensitive permissions
- [ ] Permission documentation accuracy
- [ ] RLS policy enforcement validation

## Troubleshooting

### Common Issues

**Permission not working?**
→ Check [TENANT_PERMISSIONS_DATABASE_SCHEMA.md](./TENANT_PERMISSIONS_DATABASE_SCHEMA.md) - "Query Examples"

**User can't access their data?**
→ Check [TENANT_PERMISSIONS_README.md](./TENANT_PERMISSIONS_README.md) - "Troubleshooting"

**Which permissions does role X have?**
→ Use [TENANT_PERMISSIONS_MATRIX.md](./TENANT_PERMISSIONS_MATRIX.md) - Permission Matrix

**How to structure new permission?**
→ See [TENANT_PERMISSIONS_STRUCTURE.md](./TENANT_PERMISSIONS_STRUCTURE.md) - "Permission Architecture"

## Contact & Support

For questions about:

- **Architecture**: See TENANT_PERMISSIONS_STRUCTURE.md
- **Database**: See TENANT_PERMISSIONS_DATABASE_SCHEMA.md
- **Implementation**: See TENANT_PERMISSIONS_README.md
- **Quick lookup**: See TENANT_PERMISSIONS_MATRIX.md

## Version History

| Version |    Date    | Changes                                  |
| ------- | :--------: | ---------------------------------------- |
| 1.0     | 2025-01-11 | Initial multi-tenant permissions system  |
|         |            | - 64 permissions                         |
|         |            | - 14 roles (2 global + 12 tenant-scoped) |
|         |            | - 3 sample tenants                       |
|         |            | - RLS integration                        |
|         |            | - JWT support                            |

---

**Start Reading:** Choose your audience above ↑
