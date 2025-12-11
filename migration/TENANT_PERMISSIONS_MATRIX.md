# Tenant-Specific Permissions Quick Reference Matrix

## Permission Coverage by Role

| Permission                | Global Super Admin | Global System | Tenant Admin | Tenant Manager | Tenant Staff | Tenant Member |
| ------------------------- | :----------------: | :-----------: | :----------: | :------------: | :----------: | :-----------: |
| **User Management**       |
| users.create              |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| users.read                |         ✅         |      ✅       |      ✅      |       ❌       |      ❌      |      ❌       |
| users.update              |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| users.delete              |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| **Role Management**       |
| roles.create              |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| roles.read                |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| roles.update              |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| roles.delete              |         ✅         |      ❌       |      ❌      |       ❌       |      ❌      |      ❌       |
| **Product Management**    |
| products.create           |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| products.read             |         ✅         |      ❌       |      ✅      |       ✅       |      ✅      |      ✅       |
| products.update           |         ✅         |      ❌       |      ✅      |       ✅       |      ❌      |      ❌       |
| products.delete           |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| **Inventory Management**  |
| inventory.read            |         ✅         |      ❌       |      ✅      |       ✅       |      ✅      |      ❌       |
| inventory.update          |         ✅         |      ❌       |      ✅      |       ✅       |      ❌      |      ❌       |
| **Order Management**      |
| orders.create             |         ✅         |      ❌       |      ✅      |       ❌       |      ✅      |      ✅       |
| orders.read               |         ✅         |      ✅       |      ✅      |       ✅       |      ✅      |      ✅       |
| orders.update             |         ✅         |      ❌       |      ✅      |       ✅       |      ✅      |      ❌       |
| orders.delete             |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| **Payment Management**    |
| payments.create           |         ✅         |      ❌       |      ✅      |       ❌       |      ✅      |      ❌       |
| payments.read             |         ✅         |      ✅       |      ✅      |       ❌       |      ✅      |      ❌       |
| payments.update           |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| **Member Management**     |
| members.create            |         ✅         |      ❌       |      ✅      |       ❌       |      ✅      |      ❌       |
| members.read              |         ✅         |      ✅       |      ✅      |       ✅       |      ✅      |      ✅       |
| members.update            |         ✅         |      ❌       |      ✅      |       ✅       |      ❌      |      ❌       |
| members.delete            |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| **Savings Accounts**      |
| savings.create            |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| savings.read              |         ✅         |      ❌       |      ✅      |       ✅       |      ✅      |      ✅       |
| savings.update            |         ✅         |      ❌       |      ✅      |       ✅       |      ❌      |      ❌       |
| **Loan Management**       |
| loans.create              |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| loans.read                |         ✅         |      ❌       |      ✅      |       ✅       |      ✅      |      ✅       |
| loans.update              |         ✅         |      ❌       |      ✅      |       ✅       |      ❌      |      ❌       |
| **System Administration** |
| system.config             |         ✅         |      ❌       |      ❌      |       ❌       |      ❌      |      ❌       |
| system.reports            |         ✅         |      ❌       |      ❌      |       ❌       |      ❌      |      ❌       |
| system.audit              |         ✅         |      ✅       |      ❌      |       ❌       |      ❌      |      ❌       |
| **Tenant Management**     |
| tenants.create            |         ✅         |      ❌       |      ❌      |       ❌       |      ❌      |      ❌       |
| tenants.read              |         ✅         |      ❌       |      ❌      |       ❌       |      ❌      |      ❌       |
| tenants.update            |         ✅         |      ❌       |      ❌      |       ❌       |      ❌      |      ❌       |
| tenants.delete            |         ✅         |      ❌       |      ❌      |       ❌       |      ❌      |      ❌       |
| **Domain Management**     |
| domains.create            |         ✅         |      ❌       |      ❌      |       ❌       |      ❌      |      ❌       |
| domains.read              |         ✅         |      ❌       |      ❌      |       ❌       |      ❌      |      ❌       |
| domains.update            |         ✅         |      ❌       |      ❌      |       ❌       |      ❌      |      ❌       |
| domains.delete            |         ✅         |      ❌       |      ❌      |       ❌       |      ❌      |      ❌       |
| **Dashboard & Analytics** |
| dashboard.view            |         ✅         |      ✅       |      ✅      |       ✅       |      ✅      |      ✅       |
| analytics.view            |         ✅         |      ❌       |      ✅      |       ✅       |      ❌      |      ❌       |
| reports.export            |         ✅         |      ❌       |      ✅      |       ✅       |      ✅      |      ❌       |
| **Tenant Settings**       |
| settings.general          |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| settings.security         |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| settings.notifications    |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| settings.integration      |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| **Bulk Operations**       |
| bulk.import               |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| bulk.export               |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| bulk.delete               |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| **Account Management**    |
| account.profile           |         ✅         |      ❌       |      ✅      |       ✅       |      ✅      |      ✅       |
| account.password          |         ✅         |      ❌       |      ✅      |       ✅       |      ✅      |      ✅       |
| account.sessions          |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| **Approvals & Workflow**  |
| approvals.view            |         ✅         |      ❌       |      ✅      |       ✅       |      ❌      |      ❌       |
| approvals.approve         |         ✅         |      ❌       |      ✅      |       ✅       |      ❌      |      ❌       |
| approvals.reject          |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| **Activity & Auditing**   |
| activity.log              |         ✅         |      ✅       |      ✅      |       ✅       |      ✅      |      ❌       |
| activity.export           |         ✅         |      ❌       |      ✅      |       ❌       |      ❌      |      ❌       |
| **Financial Reports**     |
| reports.financial         |         ✅         |      ❌       |      ✅      |       ✅       |      ❌      |      ❌       |
| reports.member            |         ✅         |      ❌       |      ✅      |       ✅       |      ❌      |      ❌       |
| reports.sales             |         ✅         |      ❌       |      ✅      |       ✅       |      ❌      |      ❌       |

## Permission Summary

### By Role

- **Global Super Admin:** 64/64 permissions (100%)
- **Global System:** 7/64 permissions (11%)
- **Tenant Admin:** 48/64 permissions (75%)
- **Tenant Manager:** 26/64 permissions (41%)
- **Tenant Staff:** 16/64 permissions (25%)
- **Tenant Member:** 11/64 permissions (17%)

### By Category

| Category              | Total  | Super Admin | System | Tenant Admin | Manager | Staff  | Member |
| --------------------- | :----: | :---------: | :----: | :----------: | :-----: | :----: | :----: |
| User Management       |   4    |      4      |   1    |      4       |    0    |   0    |   0    |
| Role Management       |   4    |      4      |   0    |      3       |    0    |   0    |   0    |
| Product Management    |   4    |      4      |   0    |      4       |    2    |   1    |   1    |
| Inventory             |   2    |      2      |   0    |      2       |    2    |   1    |   0    |
| Order Management      |   4    |      4      |   1    |      4       |    2    |   3    |   2    |
| Payment Management    |   3    |      3      |   1    |      3       |    0    |   1    |   0    |
| Member Management     |   4    |      4      |   1    |      4       |    2    |   2    |   1    |
| Savings               |   3    |      3      |   0    |      3       |    2    |   1    |   1    |
| Loans                 |   3    |      3      |   0    |      3       |    2    |   1    |   1    |
| System Admin          |   3    |      3      |   1    |      0       |    0    |   0    |   0    |
| Tenant Mgmt           |   4    |      4      |   0    |      0       |    0    |   0    |   0    |
| Domain Mgmt           |   4    |      4      |   0    |      0       |    0    |   0    |   0    |
| Dashboard & Analytics |   3    |      3      |   1    |      3       |    3    |   2    |   1    |
| Tenant Settings       |   4    |      4      |   0    |      4       |    0    |   0    |   0    |
| Bulk Operations       |   3    |      3      |   0    |      3       |    0    |   0    |   0    |
| Account Management    |   3    |      3      |   0    |      3       |    2    |   2    |   2    |
| Approvals             |   3    |      3      |   0    |      3       |    2    |   0    |   0    |
| Activity & Auditing   |   2    |      2      |   1    |      2       |    1    |   1    |   0    |
| Financial Reports     |   3    |      3      |   0    |      3       |    3    |   0    |   0    |
| **TOTAL**             | **64** |   **64**    | **7**  |    **48**    | **26**  | **16** | **11** |

## Common Permission Sets

### Create Product

Required roles: Admin only
Required permissions: `products.create`

### View Dashboard

Required roles: Super Admin, System, Admin, Manager, Staff, Member
Required permissions: `dashboard.view`

### Approve Loan

Required roles: Admin, Manager
Required permissions: `approvals.approve`, `loans.update`

### Generate Financial Report

Required roles: Admin, Manager
Required permissions: `reports.financial`, `analytics.view`

### Edit Own Profile

Required roles: All (Super Admin, System, Admin, Manager, Staff, Member)
Required permissions: `account.profile`

### Process Payment

Required roles: Admin, Staff
Required permissions: `payments.create`

### View Member Details

Required roles: Admin, Manager, Staff, Member
Required permissions: `members.read`

### Manage Settings

Required roles: Admin only
Required permissions: `settings.general`, `settings.security`, `settings.notifications`

## Permission Scope Matrix

| Permission   |      Scope      | Global | Tenant 1 | Tenant 2 | Tenant 3 |
| ------------ | :-------------: | :----: | :------: | :------: | :------: |
| users.\*     |     Global      |   ✅   |    -     |    -     |    -     |
| roles.\*     | Global & Tenant |   ✅   |    ✅    |    ✅    |    ✅    |
| products.\*  |     Tenant      |   -    |    ✅    |    ✅    |    ✅    |
| orders.\*    |     Tenant      |   -    |    ✅    |    ✅    |    ✅    |
| members.\*   |     Tenant      |   -    |    ✅    |    ✅    |    ✅    |
| loans.\*     |     Tenant      |   -    |    ✅    |    ✅    |    ✅    |
| savings.\*   |     Tenant      |   -    |    ✅    |    ✅    |    ✅    |
| tenants.\*   |     Global      |   ✅   |    -     |    -     |    -     |
| domains.\*   |     Global      |   ✅   |    -     |    -     |    -     |
| dashboard.\* |      Both       |   ✅   |    ✅    |    ✅    |    ✅    |
| settings.\*  |     Tenant      |   -    |    ✅    |    ✅    |    ✅    |
| reports.\*   |      Both       |   ✅   |    ✅    |    ✅    |    ✅    |

## Query Examples

### List all permissions for a specific role:

```sql
SELECT p.code, p.description
FROM app.role_permissions rp
JOIN app.permissions p ON rp.permission_id = p.id
JOIN app.roles r ON rp.role_id = r.id
WHERE r.id = '30000000-0000-0000-0000-000000000001'::uuid
ORDER BY p.code;
```

### Find users with a specific permission in a tenant:

```sql
SELECT DISTINCT u.username, u.full_name, r.name
FROM app.users u
JOIN app.user_roles ur ON u.id = ur.user_id
JOIN app.roles r ON ur.role_id = r.id
JOIN app.role_permissions rp ON r.id = rp.role_id
JOIN app.permissions p ON rp.permission_id = p.id
WHERE u.tenant_id = '20000000-0000-0000-0000-000000000001'::uuid
AND p.code = 'products.create'
ORDER BY u.username;
```

### Check permission inheritance by role hierarchy:

```sql
SELECT
  r.name as role_name,
  COUNT(rp.permission_id) as total_permissions,
  CASE
    WHEN r.tenant_id IS NULL THEN 'Global'
    ELSE 'Tenant'
  END as scope
FROM app.roles r
LEFT JOIN app.role_permissions rp ON r.id = rp.role_id
GROUP BY r.id, r.name, r.tenant_id
ORDER BY r.tenant_id, r.name;
```
