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
  'SUPER_ADMIN',
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
  'ADMIN',
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

-- ===========================
-- 5. SUBSCRIPTION PLANS
-- ===========================
-- Comprehensive pricing tiers for different cooperative sizes

INSERT INTO app.subscription_plans (name, price, currency, billing_period, max_users, max_unit_usaha, description, is_active, created_at, updated_at)
VALUES 
  -- BASIC Plan: Small cooperatives, limited features
  ('BASIC_MONTHLY', 300000.00, 'IDR', 'monthly', 5, 2, 
   'Paket dasar untuk koperasi kecil - Administrasi pesanan dan inventaris dasar', true, now(), now()),
  ('BASIC_YEARLY', 3300000.00, 'IDR', 'yearly', 5, 2,
   'Paket dasar tahunan (hemat 10%) - Administrasi pesanan dan inventaris dasar', true, now(), now()),
  
  -- PROFESSIONAL Plan: Medium cooperatives, most features
  ('PROFESSIONAL_MONTHLY', 750000.00, 'IDR', 'monthly', -1, 10,
   'Paket profesional untuk koperasi menengah - Fitur lengkap manajemen', true, now(), now()),
  ('PROFESSIONAL_YEARLY', 8250000.00, 'IDR', 'yearly', -1, 10,
   'Paket profesional tahunan (hemat 10%) - Fitur lengkap manajemen', true, now(), now()),
  
  -- ENTERPRISE Plan: Large cooperatives, all features + support
  ('ENTERPRISE_MONTHLY', 1500000.00, 'IDR', 'monthly', -1, -1,
   'Paket enterprise untuk koperasi besar - Semua fitur + dukungan prioritas', true, now(), now()),
  ('ENTERPRISE_YEARLY', 16500000.00, 'IDR', 'yearly', -1, -1,
   'Paket enterprise tahunan (hemat 10%) - Semua fitur + dukungan prioritas', true, now(), now())
ON CONFLICT (name) DO NOTHING;

-- ===========================
-- 6. SUBSCRIPTION FEATURES
-- ===========================
-- Complete feature catalog with categories for feature toggles

INSERT INTO app.subscription_features (feature_key, display_name, description, category, created_at)
VALUES
  -- Administration Features
  ('feature.user_management', 'Manajemen Pengguna', 'Kelola pengguna, password, dan profil', 'administration', now()),
  ('feature.role_management', 'Manajemen Peran & Izin', 'Atur peran dan izin akses pengguna', 'administration', now()),
  ('feature.audit_log', 'Log Audit & Aktivitas', 'Catat semua aktivitas pengguna untuk audit', 'administration', now()),
  ('feature.system_settings', 'Pengaturan Sistem', 'Konfigurasi email, timezone, keamanan', 'administration', now()),
  ('feature.company_branding', 'Branding Perusahaan', 'Ubah logo, warna, dan tema dashboard', 'administration', now()),
  
  -- Member Features
  ('feature.member_management', 'Manajemen Anggota', 'Registrasi, profil, dan data anggota koperasi', 'member', now()),
  ('feature.member_directory', 'Direktori Anggota', 'Daftar lengkap anggota dengan pencarian lanjutan', 'member', now()),
  ('feature.member_analytics', 'Analitik Anggota', 'Laporan demografi dan aktivitas anggota', 'member', now()),
  ('feature.member_portal', 'Portal Anggota Self-Service', 'Aplikasi mobile/web untuk anggota melihat rekening', 'member', now()),
  
  -- Product & Inventory Features
  ('feature.product_management', 'Manajemen Produk', 'Tambah, edit, hapus produk dan kategori', 'product', now()),
  ('feature.inventory_management', 'Manajemen Inventaris', 'Pantau stok, lokasi gudang, dan penyesuaian', 'product', now()),
  ('feature.barcode_scanning', 'Pemindaian Barcode', 'Pemindaian kode batang untuk penerimaan/pengeluaran', 'product', now()),
  ('feature.product_analytics', 'Analitik Produk', 'Laporan penjualan produk dan tren', 'product', now()),
  
  -- Supply Chain Features
  ('feature.supplier_management', 'Manajemen Supplier', 'Data supplier, kontak, dan riwayat transaksi', 'supply', now()),
  ('feature.purchase_order', 'Pesanan Pembelian', 'Buat dan kelola pesanan pembelian', 'supply', now()),
  ('feature.warehouse_management', 'Manajemen Gudang', 'Multi-lokasi gudang dengan transfer stok', 'supply', now()),
  ('feature.procurement_analytics', 'Analitik Procurement', 'Laporan pembelian dan analisis supplier', 'supply', now()),
  
  -- Sales & Order Features
  ('feature.sales_order', 'Pesanan Penjualan', 'Buat pesanan penjualan dan kelola status', 'sales', now()),
  ('feature.invoice_generation', 'Pembuatan Invoice', 'Buat dan cetak invoice otomatis', 'sales', now()),
  ('feature.order_tracking', 'Pelacakan Pesanan', 'Pantau status pesanan real-time', 'sales', now()),
  ('feature.payment_processing', 'Pemrosesan Pembayaran', 'Terima pembayaran tunai, transfer, e-wallet', 'sales', now()),
  
  -- Financial Features
  ('feature.savings_account', 'Rekening Tabungan', 'Kelola rekening tabungan anggota', 'finance', now()),
  ('feature.loan_management', 'Manajemen Pinjaman', 'Proses dan kelola pinjaman anggota', 'finance', now()),
  ('feature.loan_interest_calculation', 'Kalkulasi Bunga Pinjaman', 'Kalkulasi bunga otomatis per periode', 'finance', now()),
  ('feature.financial_accounting', 'Akuntansi Keuangan', 'Buku besar, jurnal, dan laporan keuangan', 'finance', now()),
  ('feature.budget_planning', 'Perencanaan Anggaran', 'Buat dan pantau anggaran tahunan', 'finance', now()),
  
  -- Reporting & Analytics
  ('feature.standard_reports', 'Laporan Standar', 'Laporan penjualan, inventaris, dan keuangan', 'reporting', now()),
  ('feature.custom_reports', 'Laporan Custom', 'Buat laporan kustom dengan filter lanjutan', 'reporting', now()),
  ('feature.dashboard_analytics', 'Dashboard & Analytics', 'Dashboard eksekutif dengan KPI real-time', 'reporting', now()),
  ('feature.data_export', 'Ekspor Data', 'Ekspor ke CSV, Excel, dan PDF', 'reporting', now()),
  
  -- Integration & API Features
  ('feature.api_access', 'Akses API', 'REST API untuk integrasi pihak ketiga', 'integration', now()),
  ('feature.webhook_integration', 'Integrasi Webhook', 'Webhook untuk notifikasi event real-time', 'integration', now()),
  ('feature.mobile_app', 'Aplikasi Mobile', 'iOS dan Android native apps untuk anggota', 'integration', now()),
  ('feature.third_party_integration', 'Integrasi Pihak Ketiga', 'Integrasi dengan sistem akuntansi eksternal', 'integration', now()),
  
  -- Advanced Features
  ('feature.multi_tenant', 'Multi-Tenant', 'Kelola multiple koperasi dalam satu sistem', 'advanced', now()),
  ('feature.two_factor_auth', 'Autentikasi Dua Faktor', 'Keamanan login tambahan dengan 2FA', 'advanced', now()),
  ('feature.data_backup', 'Backup & Recovery', 'Backup otomatis dan disaster recovery', 'advanced', now()),
  ('feature.api_rate_limiting', 'Rate Limiting API', 'Kontrol penggunaan API dengan rate limiting', 'advanced', now()),
  ('feature.sso_integration', 'Single Sign-On (SSO)', 'Integrasi SSO dengan sistem corporate', 'advanced', now())
ON CONFLICT (feature_key) DO NOTHING;

-- ===========================
-- 7. SUBSCRIPTION PLAN FEATURES MAPPING
-- ===========================
-- Define which features are included in each subscription plan

-- BASIC Plan (foundational features)
INSERT INTO app.subscription_plan_features (plan_id, feature_id, is_included)
SELECT 
  sp.id,
  sf.id,
  true
FROM app.subscription_plans sp
CROSS JOIN app.subscription_features sf
WHERE sp.name LIKE 'BASIC_%'
AND sf.feature_key IN (
  'feature.user_management',
  'feature.role_management',
  'feature.member_management',
  'feature.member_directory',
  'feature.product_management',
  'feature.inventory_management',
  'feature.supplier_management',
  'feature.sales_order',
  'feature.purchase_order',
  'feature.invoice_generation',
  'feature.payment_processing',
  'feature.financial_accounting',
  'feature.savings_account',
  'feature.standard_reports',
  'feature.data_export',
  'feature.company_branding',
  'feature.system_settings'
)
ON CONFLICT DO NOTHING;

-- PROFESSIONAL Plan (comprehensive features)
INSERT INTO app.subscription_plan_features (plan_id, feature_id, is_included)
SELECT 
  sp.id,
  sf.id,
  true
FROM app.subscription_plans sp
CROSS JOIN app.subscription_features sf
WHERE sp.name LIKE 'PROFESSIONAL_%'
AND sf.feature_key IN (
  'feature.user_management',
  'feature.role_management',
  'feature.audit_log',
  'feature.system_settings',
  'feature.company_branding',
  'feature.member_management',
  'feature.member_directory',
  'feature.member_analytics',
  'feature.product_management',
  'feature.inventory_management',
  'feature.barcode_scanning',
  'feature.product_analytics',
  'feature.supplier_management',
  'feature.purchase_order',
  'feature.warehouse_management',
  'feature.procurement_analytics',
  'feature.sales_order',
  'feature.invoice_generation',
  'feature.order_tracking',
  'feature.payment_processing',
  'feature.financial_accounting',
  'feature.budget_planning',
  'feature.savings_account',
  'feature.loan_management',
  'feature.standard_reports',
  'feature.custom_reports',
  'feature.dashboard_analytics',
  'feature.data_export',
  'feature.api_access',
  'feature.two_factor_auth',
  'feature.data_backup'
)
ON CONFLICT DO NOTHING;

-- ENTERPRISE Plan (all features)
INSERT INTO app.subscription_plan_features (plan_id, feature_id, is_included)
SELECT 
  sp.id,
  sf.id,
  true
FROM app.subscription_plans sp
CROSS JOIN app.subscription_features sf
WHERE sp.name LIKE 'ENTERPRISE_%'
ON CONFLICT DO NOTHING;

-- ===========================
-- 8. DASHBOARD THEMES
-- ===========================
-- Visual themes for customizable tenant dashboards

INSERT INTO app.dashboard_themes (name, display_name, description, preview_image_url, theme_config, is_active, created_at, updated_at)
VALUES
  ('default_blue', 'Blue Classic', 'Tema klasik dengan palet biru profesional yang cocok untuk koperasi tradisional',
   'https://cdn.kompu.id/themes/blue-classic-preview.png',
   '{"primary_color": "#1976D2", "secondary_color": "#0D47A1", "accent_color": "#1565C0", "text_color": "#212121", "background_color": "#FAFAFA"}'::jsonb,
   true, now(), now()),
   
  ('modern_green', 'Green Modern', 'Tema modern dengan palet hijau yang melambangkan pertumbuhan dan keberlanjutan',
   'https://cdn.kompu.id/themes/green-modern-preview.png',
   '{"primary_color": "#4CAF50", "secondary_color": "#2E7D32", "accent_color": "#43A047", "text_color": "#1B5E20", "background_color": "#F1F8E9"}'::jsonb,
   true, now(), now()),
   
  ('elegant_purple', 'Purple Elegant', 'Tema elegan dengan palet ungu yang memberikan kesan premium dan profesional',
   'https://cdn.kompu.id/themes/purple-elegant-preview.png',
   '{"primary_color": "#7B1FA2", "secondary_color": "#4A148C", "accent_color": "#8E24AA", "text_color": "#311B92", "background_color": "#F3E5F5"}'::jsonb,
   true, now(), now()),
   
  ('corporate_gray', 'Corporate Gray', 'Tema korporat dengan palet abu-abu netral untuk tampilan bisnis profesional',
   'https://cdn.kompu.id/themes/gray-corporate-preview.png',
   '{"primary_color": "#455A64", "secondary_color": "#263238", "accent_color": "#607D8B", "text_color": "#37474F", "background_color": "#ECEFF1"}'::jsonb,
   true, now(), now()),
   
  ('vibrant_orange', 'Orange Vibrant', 'Tema dinamis dengan palet oranye yang energik dan menarik untuk koperasi modern',
   'https://cdn.kompu.id/themes/orange-vibrant-preview.png',
   '{"primary_color": "#FF6F00", "secondary_color": "#E65100", "accent_color": "#FF8F00", "text_color": "#BF360C", "background_color": "#FFF3E0"}'::jsonb,
   true, now(), now()),
   
  ('serene_teal', 'Teal Serene', 'Tema menenangkan dengan palet teal yang cocok untuk industri keuangan',
   'https://cdn.kompu.id/themes/teal-serene-preview.png',
   '{"primary_color": "#00897B", "secondary_color": "#004D40", "accent_color": "#009688", "text_color": "#004D40", "background_color": "#E0F2F1"}'::jsonb,
   true, now(), now()),
   
  ('sunset_red', 'Red Sunset', 'Tema hangat dengan palet merah yang berani dan menarik perhatian',
   'https://cdn.kompu.id/themes/red-sunset-preview.png',
   '{"primary_color": "#D32F2F", "secondary_color": "#B71C1C", "accent_color": "#F44336", "text_color": "#C62828", "background_color": "#FFEBEE"}'::jsonb,
   true, now(), now()),
   
  ('minimal_white', 'Minimal White', 'Tema minimalis dengan latar putih untuk fokus pada konten dan data',
   'https://cdn.kompu.id/themes/minimal-white-preview.png',
   '{"primary_color": "#000000", "secondary_color": "#424242", "accent_color": "#616161", "text_color": "#212121", "background_color": "#FFFFFF"}'::jsonb,
   true, now(), now()),
   
  ('dark_slate', 'Dark Slate', 'Tema gelap dengan palet slate yang mengurangi kelelahan mata untuk penggunaan malam',
   'https://cdn.kompu.id/themes/dark-slate-preview.png',
   '{"primary_color": "#37474F", "secondary_color": "#263238", "accent_color": "#455A64", "text_color": "#ECEFF1", "background_color": "#212121"}'::jsonb,
   true, now(), now()),
   
  ('pastel_blend', 'Pastel Blend', 'Tema pastel dengan kombinasi warna lembut yang modern dan user-friendly',
   'https://cdn.kompu.id/themes/pastel-blend-preview.png',
   '{"primary_color": "#AB47BC", "secondary_color": "#6A1B9A", "accent_color": "#CE93D8", "text_color": "#4A148C", "background_color": "#F3E5F5"}'::jsonb,
   true, now(), now())
ON CONFLICT (name) DO NOTHING;

-- Link default theme to tenants (optional - set via app config or tenant settings)
-- This allows new tenants to default to 'default_blue' theme
-- Further implementation depends on how you want to handle default theme assignment