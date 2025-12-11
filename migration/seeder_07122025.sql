-- 2025-12-07_seeder_koperasi_multitenant.sql
-- Database seeder script to initialize tables with sample data
-- Run this AFTER initial_07122025.sql has been executed successfully

-- ===========================
-- 1. GLOBAL PERMISSIONS (System-wide permissions)
-- ===========================
-- These are the fundamental permissions that all roles can use

INSERT INTO app.permissions (id, code, description, created_at) VALUES
  -- User Management
  (gen_random_uuid(), 'users.create', 'Create new user account', now()),
  (gen_random_uuid(), 'users.read', 'View user account details', now()),
  (gen_random_uuid(), 'users.update', 'Edit user account information', now()),
  (gen_random_uuid(), 'users.delete', 'Delete/deactivate user account', now()),
  
  -- Role Management
  (gen_random_uuid(), 'roles.create', 'Create new role', now()),
  (gen_random_uuid(), 'roles.read', 'View roles', now()),
  (gen_random_uuid(), 'roles.update', 'Edit role permissions', now()),
  (gen_random_uuid(), 'roles.delete', 'Delete role', now()),
  
  -- Product Management
  (gen_random_uuid(), 'products.create', 'Create new product', now()),
  (gen_random_uuid(), 'products.read', 'View products and catalog', now()),
  (gen_random_uuid(), 'products.update', 'Edit product information and pricing', now()),
  (gen_random_uuid(), 'products.delete', 'Delete product', now()),
  
  -- Inventory Management
  (gen_random_uuid(), 'inventory.read', 'View inventory levels', now()),
  (gen_random_uuid(), 'inventory.update', 'Adjust inventory quantities', now()),
  
  -- Order Management
  (gen_random_uuid(), 'orders.create', 'Create new order', now()),
  (gen_random_uuid(), 'orders.read', 'View orders', now()),
  (gen_random_uuid(), 'orders.update', 'Edit order status and details', now()),
  (gen_random_uuid(), 'orders.delete', 'Cancel/delete order', now()),
  
  -- Payment Management
  (gen_random_uuid(), 'payments.read', 'View payment records', now()),
  (gen_random_uuid(), 'payments.create', 'Process payments', now()),
  (gen_random_uuid(), 'payments.update', 'Update payment status', now()),
  
  -- Member Management
  (gen_random_uuid(), 'members.create', 'Register new member', now()),
  (gen_random_uuid(), 'members.read', 'View member information', now()),
  (gen_random_uuid(), 'members.update', 'Edit member details', now()),
  (gen_random_uuid(), 'members.delete', 'Remove member', now()),
  
  -- Financial Services
  (gen_random_uuid(), 'savings.read', 'View savings accounts', now()),
  (gen_random_uuid(), 'savings.create', 'Open savings account', now()),
  (gen_random_uuid(), 'savings.update', 'Record savings transactions', now()),
  
  (gen_random_uuid(), 'loans.read', 'View loan records', now()),
  (gen_random_uuid(), 'loans.create', 'Create new loan', now()),
  (gen_random_uuid(), 'loans.update', 'Update loan status and payments', now()),
  
  -- System/Admin
  (gen_random_uuid(), 'system.config', 'Manage system configuration', now()),
  (gen_random_uuid(), 'system.reports', 'Generate reports', now()),
  (gen_random_uuid(), 'system.audit', 'View audit logs and activity', now()),
  
  -- Tenant Management (Super Admin only)
  (gen_random_uuid(), 'tenants.create', 'Create new tenant organization', now()),
  (gen_random_uuid(), 'tenants.read', 'View tenant information', now()),
  (gen_random_uuid(), 'tenants.update', 'Edit tenant settings and metadata', now()),
  (gen_random_uuid(), 'tenants.delete', 'Delete/archive tenant', now()),

  -- Dashboard & Analytics
  (gen_random_uuid(), 'dashboard.view', 'Access tenant dashboard and analytics', now()),
  (gen_random_uuid(), 'analytics.view', 'View detailed analytics and statistics', now()),
  (gen_random_uuid(), 'reports.export', 'Export reports to CSV/PDF', now()),
  
  -- Tenant Settings
  (gen_random_uuid(), 'settings.general', 'Manage general tenant settings', now()),
  (gen_random_uuid(), 'settings.security', 'Manage security and authentication settings', now()),
  (gen_random_uuid(), 'settings.notifications', 'Configure notification preferences', now()),
  (gen_random_uuid(), 'settings.integration', 'Manage third-party integrations', now()),
  
  -- Bulk Operations
  (gen_random_uuid(), 'bulk.import', 'Import bulk data (members, products)', now()),
  (gen_random_uuid(), 'bulk.export', 'Export data in bulk', now()),
  (gen_random_uuid(), 'bulk.delete', 'Delete records in bulk', now()),
  
  -- Account Management
  (gen_random_uuid(), 'account.profile', 'Manage own user profile and settings', now()),
  (gen_random_uuid(), 'account.password', 'Change password', now()),
  (gen_random_uuid(), 'account.sessions', 'Manage active sessions/devices', now()),
  
  -- Approval/Workflow
  (gen_random_uuid(), 'approvals.view', 'View pending approvals and requests', now()),
  (gen_random_uuid(), 'approvals.approve', 'Approve requests (loans, registrations)', now()),
  (gen_random_uuid(), 'approvals.reject', 'Reject/deny requests', now()),
  
  -- Activity & Auditing
  (gen_random_uuid(), 'activity.log', 'View activity logs', now()),
  (gen_random_uuid(), 'activity.export', 'Export activity logs', now()),
  
  -- Financial Reports
  (gen_random_uuid(), 'reports.financial', 'Generate financial reports', now()),
  (gen_random_uuid(), 'reports.member', 'Generate member reports', now()),
  (gen_random_uuid(), 'reports.sales', 'Generate sales/order reports', now())
ON CONFLICT DO NOTHING;

-- ===========================
-- 2. GLOBAL ROLES (System-wide roles, tenant_id = NULL)
-- ===========================

-- Super Admin Role (Global)
INSERT INTO app.roles (id, tenant_id, name, description, is_system, created_at, updated_at)
VALUES (
  '10000000-0000-0000-0000-000000000001'::uuid,
  NULL,
  'Super Admin',
  'System administrator with full access to all tenants and configurations',
  true,
  now(),
  now()
) ON CONFLICT DO NOTHING;

-- System Role for internal operations
INSERT INTO app.roles (id, tenant_id, name, description, is_system, created_at, updated_at)
VALUES (
  '10000000-0000-0000-0000-000000000002'::uuid,
  NULL,
  'System',
  'System internal account for background jobs and integrations',
  true,
  now(),
  now()
) ON CONFLICT DO NOTHING;

-- Assign all permissions to Super Admin (global access)
INSERT INTO app.role_permissions (role_id, permission_id)
SELECT '10000000-0000-0000-0000-000000000001'::uuid, id FROM app.permissions
ON CONFLICT DO NOTHING;

-- Assign essential permissions to Admin role (tenant-specific roles will be created per tenant later)
INSERT INTO app.role_permissions (role_id, permission_id)
SELECT '10000000-0000-0000-0000-000000000002'::uuid, id 
FROM app.permissions 
WHERE code IN ('users.create', 'users.read', 'users.update', 'users.delete',
               'roles.create', 'roles.read', 'roles.update', 'roles.delete',
               'products.create', 'products.read', 'products.update', 'products.delete',
               'orders.create', 'orders.read', 'orders.update', 'orders.delete',
               'payments.read', 'payments.create', 'payments.update',
               'members.create', 'members.read', 'members.update', 'members.delete',
               'savings.read', 'savings.create', 'savings.update',
               'loans.read', 'loans.create', 'loans.update',
               'system.config', 'system.reports', 'system.audit',
               'tenants.create', 'tenants.read', 'tenants.update', 'tenants.delete')
ON CONFLICT DO NOTHING;

-- Assign essential permissions to Producer/Supplier role (producer/supplier-specific roles will be created per tenant later)
INSERT INTO app.role_permissions (role_id, permission_id)
SELECT '10000000-0000-0000-0000-000000000003'::uuid, id 
FROM app.permissions 
WHERE code IN ('products.create', 'products.read', 'products.update', 'products.delete',
               'inventory.read', 'inventory.update',
               'orders.read', 'orders.update',
               'payments.read', 'payments.create', 'payments.update',
               'dashboard.view', 'analytics.view')
ON CONFLICT DO NOTHING;

-- Assign essential permissions to Customer/Member role (customer/member-specific roles will be created per tenant later)
INSERT INTO app.role_permissions (role_id, permission_id)
SELECT '10000000-0000-0000-0000-000000000004'::uuid, id 
FROM app.permissions 
WHERE code IN ('products.read',
               'orders.create', 'orders.read', 'orders.update',
               'payments.read', 'payments.create', 'payments.update',
               'members.read',
               'dashboard.view', 'analytics.view')
ON CONFLICT DO NOTHING;

-- ===========================
-- 3. FEATURE FLAGS
-- ===========================

INSERT INTO app.feature_flags (id, tenant_id, key, value, enabled, created_at, updated_at)
VALUES 
  -- Global flags
  (gen_random_uuid(), NULL, 'enable_two_factor_auth', 'true'::jsonb, true, now(), now()),
  (gen_random_uuid(), NULL, 'enable_social_login', 'false'::jsonb, false, now(), now()),
  (gen_random_uuid(), NULL, 'enable_api_access', 'true'::jsonb, true, now(), now()),
  (gen_random_uuid(), NULL, 'maintenance_mode', 'false'::jsonb, false, now(), now())
ON CONFLICT DO NOTHING;

-- ===========================
-- 4. APP CONFIGURATION
-- ===========================

INSERT INTO app.app_config (id, config_key, config_value, description, created_at, updated_at)
VALUES 
  (gen_random_uuid(), 'app.version', '1.0.0', 'Application version', now(), now()),
  (gen_random_uuid(), 'app.environment', 'development', 'Environment (dev/staging/prod)', now(), now()),
  (gen_random_uuid(), 'app.max_login_attempts', '5', 'Maximum failed login attempts before lockout', now(), now()),
  (gen_random_uuid(), 'app.lockout_duration_minutes', '30', 'Account lockout duration in minutes', now(), now()),
  (gen_random_uuid(), 'jwt.access_token_expiry', '3600', 'JWT access token expiry in seconds (1 hour)', now(), now()),
  (gen_random_uuid(), 'jwt.refresh_token_expiry', '604800', 'JWT refresh token expiry in seconds (7 days)', now(), now()),
  (gen_random_uuid(), 'password.min_length', '8', 'Minimum password length', now(), now()),
  (gen_random_uuid(), 'password.require_uppercase', 'true', 'Password must contain uppercase letter', now(), now()),
  (gen_random_uuid(), 'password.require_numbers', 'true', 'Password must contain numbers', now(), now()),
  (gen_random_uuid(), 'password.require_special', 'false', 'Password must contain special characters', now(), now()),
  (gen_random_uuid(), 'email.smtp_host', 'smtp.gmail.com', 'SMTP server host for email notifications', now(), now()),
  (gen_random_uuid(), 'email.smtp_port', '587', 'SMTP server port', now(), now()),
  (gen_random_uuid(), 'email.from_address', 'noreply@kompu.id', 'Default from email address', now(), now()),
  (gen_random_uuid(), 'payment.currency_default', 'IDR', 'Default currency for payments', now(), now()),
  (gen_random_uuid(), 'timezone', 'Asia/Jakarta', 'Default application timezone', now(), now())
ON CONFLICT DO NOTHING;