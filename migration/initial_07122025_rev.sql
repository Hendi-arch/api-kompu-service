-- 2025-12-07_koperasi_multitenant_schema.sql
-- Organized flow: Master Entities → Registration Entities → Authentication → Business Operations
-- Use with Flyway or Liquibase. Each CREATE can be separated into migrations.

-- ============================================================================
-- PHASE 1: CORE INFRASTRUCTURE & SETUP
-- ============================================================================

-- === Extensions ===
CREATE EXTENSION IF NOT EXISTS "pgcrypto";    -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pg_trgm";     -- for text search indexing if required

-- === Schemas ===
CREATE SCHEMA IF NOT EXISTS app AUTHORIZATION postgres;

SET search_path = app, public;

-- === Core Audit Trigger Function ===
-- Automatically fills created_at, updated_at, created_by, updated_by on INSERT/UPDATE
CREATE OR REPLACE FUNCTION app.set_audit_fields() RETURNS trigger AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    NEW.created_at := coalesce(NEW.created_at, now());
    NEW.updated_at := coalesce(NEW.updated_at, now());
    IF NEW.created_by IS NULL THEN
      NEW.created_by := app.current_user_id();
    END IF;
    IF NEW.updated_by IS NULL THEN
      NEW.updated_by := app.current_user_id();
    END IF;
  ELSE
    NEW.updated_at := now();
    NEW.updated_by := app.current_user_id();
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- PHASE 2: MASTER/REFERENCE ENTITIES (Config data created before registration)
-- ============================================================================

-- === 2.1: Subscription Plans - Available pricing tiers ===
CREATE TABLE app.subscription_plans (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name text NOT NULL UNIQUE, -- 'BASIC', 'PRO', 'ENTERPRISE'
  price numeric(14,2) NOT NULL,
  currency text NOT NULL DEFAULT 'IDR',
  billing_period text NOT NULL DEFAULT 'monthly', -- 'monthly', 'yearly'
  max_users integer DEFAULT -1, -- -1 = unlimited
  max_unit_usaha integer DEFAULT 2, -- business units allowed per plan
  description text,
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_subscription_plans_active ON app.subscription_plans(is_active);

-- === 2.2: Subscription Features - Feature catalog ===
CREATE TABLE app.subscription_features (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  feature_key text NOT NULL UNIQUE, -- e.g., 'feature.member_portal', 'feature.simpan_pinjam'
  display_name text NOT NULL,
  description text,
  category text, -- 'administration', 'member', 'supply', 'finance', 'reporting'
  created_at timestamptz NOT NULL DEFAULT now()
);

-- === 2.3: Subscription Plan Features Mapping ===
CREATE TABLE app.subscription_plan_features (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  plan_id uuid NOT NULL REFERENCES app.subscription_plans(id) ON DELETE CASCADE,
  feature_id uuid NOT NULL REFERENCES app.subscription_features(id) ON DELETE CASCADE,
  is_included boolean NOT NULL DEFAULT true,
  PRIMARY KEY (plan_id, feature_id)
);

CREATE INDEX idx_plan_features_plan ON app.subscription_plan_features(plan_id);

-- === 2.4: Dashboard Themes - Visual customization templates ===
CREATE TABLE IF NOT EXISTS app.dashboard_themes (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name text NOT NULL UNIQUE, -- 'tema1', 'tema2', 'tema3'
  display_name text NOT NULL,
  description text,
  preview_image_url text,
  theme_config jsonb DEFAULT '{}'::jsonb,
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_dashboard_themes_active ON app.dashboard_themes(is_active);

-- === 2.5: Permissions - Granular permission definitions ===
CREATE TABLE app.permissions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  code text NOT NULL UNIQUE, -- e.g. products.create, orders.read
  description text,
  created_at timestamptz NOT NULL DEFAULT now()
);

-- === 2.6: Roles - RBAC role definitions (global or tenant-scoped) ===
CREATE TABLE app.roles (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid REFERENCES app.tenants(id) ON DELETE CASCADE, -- NULL => global role
  name text NOT NULL,
  description text,
  is_system boolean DEFAULT false,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_roles_tenant_name ON app.roles(tenant_id, lower(name));

-- === 2.7: Role Permissions Mapping ===
CREATE TABLE app.role_permissions (
  role_id uuid NOT NULL REFERENCES app.roles(id) ON DELETE CASCADE,
  permission_id uuid NOT NULL REFERENCES app.permissions(id) ON DELETE CASCADE,
  PRIMARY KEY (role_id, permission_id)
);

-- === 2.8: Feature Flags - Enable/disable features per tenant or globally ===
CREATE TABLE app.feature_flags (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid REFERENCES app.tenants(id) ON DELETE CASCADE, -- NULL => global flag
  key text NOT NULL,
  value jsonb NOT NULL,
  enabled boolean DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_flags_tenant_key ON app.feature_flags(tenant_id, lower(key));

-- === 2.9: Application Configuration ===
CREATE TABLE IF NOT EXISTS app.app_config (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  config_key text NOT NULL UNIQUE,
  config_value text NOT NULL,
  description text,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_app_config_key ON app.app_config(lower(config_key));
CREATE INDEX idx_app_config_created_at ON app.app_config(created_at DESC);

-- ============================================================================
-- PHASE 3: TENANT & REGISTRATION ENTITIES (Created during signup/registration)
-- ============================================================================

-- === 3.1: Tenant - Core entity created during registration ===
CREATE TABLE app.tenants (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name text NOT NULL,
  code text UNIQUE, -- short code / custom URL slug
  status text NOT NULL DEFAULT 'active', -- active | suspended | archived
  metadata jsonb DEFAULT '{}'::jsonb, -- flexible storage for logo_url, theme_preference, etc.
  theme_id uuid REFERENCES app.dashboard_themes(id), -- link to themes table
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by uuid,
  updated_by uuid,
  deleted_at timestamptz
);

CREATE INDEX IF NOT EXISTS idx_tenants_status ON app.tenants(status);
CREATE INDEX IF NOT EXISTS idx_tenants_code ON app.tenants(lower(code));

-- === 3.2: Tenant Domains - Custom domain mapping ===
CREATE TABLE app.tenant_domains (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  host text NOT NULL, -- e.g. koperasi1.kompu.id or www.koperasi1.com
  is_primary boolean DEFAULT false,
  is_custom boolean DEFAULT false,
  https_enabled boolean DEFAULT true,
  tls_provider text DEFAULT 'cloudflare',
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
CREATE UNIQUE INDEX idx_tenant_domains_host ON app.tenant_domains(lower(host));
CREATE INDEX idx_tenant_domains_tenant ON app.tenant_domains(tenant_id);

-- === 3.3: Tenant Registrations - Registration audit & compliance trail ===
CREATE TABLE IF NOT EXISTS app.tenant_registrations (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL UNIQUE REFERENCES app.tenants(id) ON DELETE CASCADE,
  registration_type text NOT NULL, -- 'koperasi', 'non-koperasi'
  admin_user_id uuid, -- will be linked after user creation
  email_used text NOT NULL,
  ip_address inet,
  user_agent text,
  terms_accepted_at timestamptz,
  privacy_accepted_at timestamptz,
  email_verification_sent_at timestamptz,
  email_verified_at timestamptz,
  registration_source text DEFAULT 'web', -- 'web', 'api', 'mobile', 'manual'
  notes text,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_tenant_registrations_created_at ON app.tenant_registrations(created_at DESC);
CREATE INDEX idx_tenant_registrations_email ON app.tenant_registrations(lower(email_used));

-- === 3.4: Tenant Subscriptions - Active subscription tracking ===
CREATE TABLE app.tenant_subscriptions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL UNIQUE REFERENCES app.tenants(id) ON DELETE CASCADE,
  plan_id uuid NOT NULL REFERENCES app.subscription_plans(id),
  subscription_start_date date NOT NULL DEFAULT CURRENT_DATE,
  subscription_end_date date,
  status text NOT NULL DEFAULT 'active', -- 'trial', 'active', 'suspended', 'cancelled', 'expired'
  auto_renew boolean NOT NULL DEFAULT true,
  trial_ends_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by uuid,
  updated_by uuid
);

CREATE INDEX idx_tenant_subscriptions_status ON app.tenant_subscriptions(status);
CREATE INDEX idx_tenant_subscriptions_plan ON app.tenant_subscriptions(plan_id);
CREATE INDEX idx_tenant_subscriptions_tenant ON app.tenant_subscriptions(tenant_id);

-- === 3.5: Subscription Invoices - Billing history ===
CREATE TABLE app.subscription_invoices (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  subscription_id uuid NOT NULL REFERENCES app.tenant_subscriptions(id),
  invoice_number text NOT NULL,
  amount numeric(14,2) NOT NULL,
  currency text NOT NULL DEFAULT 'IDR',
  billing_period_start date NOT NULL,
  billing_period_end date NOT NULL,
  status text NOT NULL DEFAULT 'draft', -- 'draft', 'sent', 'paid', 'failed', 'refunded'
  issued_at timestamptz NOT NULL DEFAULT now(),
  due_at timestamptz NOT NULL,
  paid_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_subscription_invoices_number ON app.subscription_invoices(tenant_id, invoice_number);
CREATE INDEX idx_subscription_invoices_status ON app.subscription_invoices(status);
CREATE INDEX idx_subscription_invoices_tenant ON app.subscription_invoices(tenant_id, issued_at DESC);

-- ============================================================================
-- PHASE 4: AUTHENTICATION & USER MANAGEMENT
-- ============================================================================

-- === 3.1: Users - Core user accounts (global or tenant-scoped) ===
CREATE TABLE app.users (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid REFERENCES app.tenants(id) ON DELETE CASCADE, -- NULL => global (super admin) account
  email text NOT NULL,
  password_hash text, -- managed by app; nullable for SSO users
  full_name text,
  phone text,
  avatar_url text,
  is_active boolean DEFAULT true,
  is_email_verified boolean DEFAULT false,
  is_system boolean DEFAULT false, -- internal system accounts
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by uuid,
  updated_by uuid,
  deleted_at timestamptz
);
CREATE UNIQUE INDEX idx_users_email_tenant ON app.users (tenant_id, lower(email));

-- === 3.2: User Sessions - Track login sessions per device/IP ===
CREATE TABLE IF NOT EXISTS app.user_sessions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid REFERENCES app.tenants(id) ON DELETE CASCADE, -- NULL for super admin
  user_id uuid NOT NULL REFERENCES app.users(id) ON DELETE CASCADE,
  ip inet,
  user_agent text,
  created_at timestamptz NOT NULL DEFAULT now(),
  last_active_at timestamptz NOT NULL DEFAULT now(),
  is_active boolean NOT NULL DEFAULT true,
  deleted_at timestamptz
);

CREATE INDEX idx_user_sessions_user ON app.user_sessions(user_id);
CREATE INDEX idx_user_sessions_tenant ON app.user_sessions(tenant_id);

-- === 3.3: Refresh Tokens - Secure token-based session management ===
CREATE TABLE IF NOT EXISTS app.refresh_tokens (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES app.users(id) ON DELETE CASCADE,
  session_id uuid REFERENCES app.user_sessions(id) ON DELETE SET NULL,
  token_hash bytea NOT NULL, -- store hash, not plaintext
  created_at timestamptz NOT NULL DEFAULT now(),
  expires_at timestamptz NOT NULL,
  revoked_at timestamptz,
  CONSTRAINT ux_refresh_user_hash UNIQUE (user_id, token_hash)
);

CREATE INDEX idx_refresh_tokens_user ON app.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash ON app.refresh_tokens(token_hash);

-- === 3.4: Revoked JTIs - Blacklist for revoked JWT access tokens ===
CREATE TABLE IF NOT EXISTS app.revoked_jtis (
  jti uuid PRIMARY KEY,
  user_id uuid REFERENCES app.users(id) ON DELETE SET NULL,
  revoked_at timestamptz NOT NULL DEFAULT now(),
  expires_at timestamptz NOT NULL
);

CREATE INDEX idx_revoked_jtis_user ON app.revoked_jtis(user_id);

-- === 3.5: Roles - RBAC role definitions (global or tenant-scoped) ===
CREATE TABLE app.roles (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid REFERENCES app.tenants(id) ON DELETE CASCADE, -- NULL => global role (super-admin roles)
  name text NOT NULL,
  description text,
  is_system boolean DEFAULT false,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_roles_tenant_name ON app.roles(tenant_id, lower(name));

-- === 3.6: Permissions - Granular permission definitions ===
CREATE TABLE app.permissions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  code text NOT NULL UNIQUE, -- e.g. products.create, orders.read
  description text,
  created_at timestamptz NOT NULL DEFAULT now()
);

-- === 3.7: Role Permissions Mapping ===
CREATE TABLE app.role_permissions (
  role_id uuid NOT NULL REFERENCES app.roles(id) ON DELETE CASCADE,
  permission_id uuid NOT NULL REFERENCES app.permissions(id) ON DELETE CASCADE,
  PRIMARY KEY (role_id, permission_id)
);

-- === 3.8: User Roles Assignment ===
CREATE TABLE app.user_roles (
  user_id uuid NOT NULL REFERENCES app.users(id) ON DELETE CASCADE,
  role_id uuid NOT NULL REFERENCES app.roles(id) ON DELETE CASCADE,
  assigned_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, role_id)
);

-- ============================================================================
-- PHASE 5: BUSINESS SETUP & MEMBERS
-- ============================================================================

-- === 4.1: Members / Employees - Tenant member directory ===
CREATE TABLE app.members (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  member_code text,
  user_id uuid REFERENCES app.users(id) ON DELETE SET NULL,
  full_name text NOT NULL,
  email text,
  phone text,
  address text,
  joined_at date,
  status text DEFAULT 'active',
  metadata jsonb DEFAULT '{}'::jsonb,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by uuid,
  updated_by uuid,
  deleted_at timestamptz
);
CREATE INDEX idx_members_tenant_member_code ON app.members(tenant_id, member_code);
CREATE INDEX idx_members_tenant_fullname ON app.members(tenant_id, lower(full_name) text_pattern_ops);

-- ============================================================================
-- PHASE 6: PRODUCT CATALOG & INVENTORY
-- ============================================================================

-- === 5.1: Product Categories - Product taxonomy ===
CREATE TABLE app.product_categories (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  name text NOT NULL,
  slug text NOT NULL,
  parent_id uuid REFERENCES app.product_categories(id) ON DELETE SET NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
CREATE UNIQUE INDEX idx_cat_tenant_slug ON app.product_categories(tenant_id, lower(slug));

-- === 5.2: Products - Product master data ===
CREATE TABLE app.products (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  sku text,
  name text NOT NULL,
  description text,
  category_id uuid REFERENCES app.product_categories(id) ON DELETE SET NULL,
  price numeric(14,2) NOT NULL DEFAULT 0,
  cost_price numeric(14,2),
  weight_grams integer,
  is_active boolean DEFAULT true,
  metadata jsonb DEFAULT '{}'::jsonb,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by uuid,
  updated_by uuid,
  deleted_at timestamptz
);
CREATE INDEX idx_products_tenant_name ON app.products(tenant_id, lower(name));
CREATE INDEX idx_products_tenant_sku ON app.products(tenant_id, lower(sku));
CREATE INDEX idx_products_tenant_price ON app.products(tenant_id, price);
CREATE INDEX idx_products_metadata_gin ON app.products USING gin (metadata);

-- === 5.3: Inventories - Stock tracking per location ===
CREATE TABLE app.inventories (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  product_id uuid NOT NULL REFERENCES app.products(id) ON DELETE CASCADE,
  location text, -- warehouse or branch id
  quantity bigint NOT NULL DEFAULT 0,
  reserved bigint NOT NULL DEFAULT 0,
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
CREATE UNIQUE INDEX idx_inventories_unique ON app.inventories(tenant_id, product_id, location);

-- ============================================================================
-- PHASE 7: SUPPLY CHAIN & SUPPLIERS
-- ============================================================================

-- === 6.1: Suppliers / Vendors - Supplier management ===
CREATE TABLE app.suppliers (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  supplier_code text NOT NULL,
  supplier_name text NOT NULL,
  supplier_type text, -- 'producer', 'distributor', 'wholesaler'
  contact_person text,
  email text,
  phone text,
  address text,
  city text,
  province text,
  postal_code text,
  bank_account text,
  bank_name text,
  tax_id text, -- NPWP/VAT ID
  status text NOT NULL DEFAULT 'active', -- 'active', 'inactive', 'blacklisted'
  rating numeric(2,1), -- 1-5 stars
  payment_terms text, -- e.g., 'Net 30', 'COD'
  notes text,
  metadata jsonb DEFAULT '{}'::jsonb,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by uuid,
  updated_by uuid,
  deleted_at timestamptz
);

CREATE UNIQUE INDEX idx_suppliers_tenant_code ON app.suppliers(tenant_id, supplier_code);
CREATE INDEX idx_suppliers_status ON app.suppliers(status);

-- === 6.2: Supplier Product Prices - Price comparison & sourcing ===
CREATE TABLE app.supplier_product_prices (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  supplier_id uuid NOT NULL REFERENCES app.suppliers(id) ON DELETE CASCADE,
  product_id uuid NOT NULL REFERENCES app.products(id) ON DELETE CASCADE,
  unit_price numeric(14,2) NOT NULL,
  currency text NOT NULL DEFAULT 'IDR',
  minimum_order_qty integer DEFAULT 1,
  maximum_order_qty integer,
  lead_time_days integer, -- delivery time
  is_active boolean NOT NULL DEFAULT true,
  valid_from date NOT NULL DEFAULT CURRENT_DATE,
  valid_until date,
  notes text,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_supplier_prices_unique ON app.supplier_product_prices(supplier_id, product_id, valid_from);
CREATE INDEX idx_supplier_prices_product ON app.supplier_product_prices(product_id, unit_price);
CREATE INDEX idx_supplier_prices_tenant ON app.supplier_product_prices(tenant_id);

-- ============================================================================
-- PHASE 8: SALES & ORDERS
-- ============================================================================

-- === 7.1: Orders - Sales and purchase orders ===
CREATE TABLE app.orders (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  order_number text NOT NULL,
  order_type text DEFAULT 'sales', -- 'sales' or 'purchase'
  buyer_id uuid REFERENCES app.members(id) ON DELETE SET NULL, -- for sales orders
  supplier_id uuid REFERENCES app.suppliers(id) ON DELETE SET NULL, -- for purchase orders
  buyer_snapshot jsonb, -- store denormalized buyer details
  status text NOT NULL DEFAULT 'created',
  total_amount numeric(14,2) NOT NULL DEFAULT 0,
  currency text NOT NULL DEFAULT 'IDR',
  metadata jsonb DEFAULT '{}'::jsonb,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by uuid,
  updated_by uuid,
  deleted_at timestamptz
);

CREATE UNIQUE INDEX idx_orders_tenant_order_number ON app.orders(tenant_id, order_number);
CREATE INDEX idx_orders_tenant_created_at ON app.orders(tenant_id, created_at DESC);
CREATE INDEX idx_orders_supplier ON app.orders(supplier_id);
CREATE INDEX idx_orders_buyer ON app.orders(buyer_id);

-- === 7.2: Order Items - Line items for orders ===
CREATE TABLE app.order_items (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id uuid NOT NULL REFERENCES app.orders(id) ON DELETE CASCADE,
  product_id uuid REFERENCES app.products(id) ON DELETE SET NULL,
  product_snapshot jsonb,
  quantity integer NOT NULL DEFAULT 1,
  unit_price numeric(14,2) NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);

CREATE INDEX idx_order_items_orderid ON app.order_items(order_id);

-- === 7.3: Payments - Payment records for orders ===
CREATE TABLE app.payments (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  order_id uuid REFERENCES app.orders(id) ON DELETE SET NULL,
  payment_provider text,
  provider_reference text,
  amount numeric(14,2) NOT NULL,
  currency text NOT NULL DEFAULT 'IDR',
  status text NOT NULL DEFAULT 'pending',
  paid_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
CREATE INDEX idx_payments_tenant_order ON app.payments(tenant_id, order_id);
CREATE INDEX idx_payments_status ON app.payments(status);

-- ============================================================================
-- PHASE 9: FINANCIAL PRODUCTS & ACCOUNTING
-- ============================================================================

-- === 8.1: Savings Accounts - Member savings tracking ===
CREATE TABLE app.savings_accounts (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  member_id uuid REFERENCES app.members(id) ON DELETE CASCADE,
  account_number text NOT NULL,
  balance numeric(18,2) NOT NULL DEFAULT 0,
  status text DEFAULT 'active',
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
CREATE UNIQUE INDEX idx_savings_tenant_acc ON app.savings_accounts(tenant_id, account_number);

-- === 8.2: Loans - Member loan management ===
CREATE TABLE app.loans (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  member_id uuid REFERENCES app.members(id) ON DELETE CASCADE,
  loan_number text NOT NULL,
  principal numeric(18,2),
  outstanding numeric(18,2),
  status text DEFAULT 'draft',
  terms jsonb,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
CREATE UNIQUE INDEX idx_loans_tenant_number ON app.loans(tenant_id, loan_number);

-- ============================================================================
-- PHASE 10: DOCUMENT MANAGEMENT
-- ============================================================================

-- === 9.1: Documents - Document generation tracking (PO, Invoice, etc.) ===
CREATE TABLE app.documents (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  document_type text NOT NULL, -- 'PO', 'INVOICE', 'RECEIPT', 'SHIPPING_LETTER', 'GOODS_RECEIPT', 'RETURN_NOTE'
  document_number text NOT NULL,
  order_id uuid REFERENCES app.orders(id) ON DELETE SET NULL,
  supplier_id uuid REFERENCES app.suppliers(id) ON DELETE SET NULL,
  content_path text, -- S3 path, file server path, etc.
  content_format text DEFAULT 'pdf', -- 'pdf', 'xml', 'json'
  generated_at timestamptz NOT NULL DEFAULT now(),
  sent_at timestamptz,
  signature_hash text, -- for document verification
  status text DEFAULT 'generated', -- 'generated', 'sent', 'verified', 'archived'
  created_at timestamptz NOT NULL DEFAULT now(),
  created_by uuid REFERENCES app.users(id),
  deleted_at timestamptz
);

CREATE UNIQUE INDEX idx_documents_number ON app.documents(tenant_id, document_type, document_number);
CREATE INDEX idx_documents_order ON app.documents(order_id);
CREATE INDEX idx_documents_created_at ON app.documents(created_at DESC);

-- ============================================================================
-- PHASE 11: SYSTEM & MONITORING
-- ============================================================================

-- === 10.1: Activity Log - Comprehensive audit trail for all actions ===
CREATE TABLE app.activity_log (
  id bigserial PRIMARY KEY,
  tenant_id uuid,
  user_id uuid,
  actor_snapshot jsonb,
  action text NOT NULL,
  resource_type text,
  resource_id uuid,
  payload jsonb,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_activity_tenant ON app.activity_log (tenant_id, created_at DESC);
CREATE INDEX idx_activity_user ON app.activity_log (user_id, created_at DESC);

-- === 10.2: Login Log - Track authentication events ===
CREATE TABLE app.login_log (
  id bigserial PRIMARY KEY,
  tenant_id uuid,
  user_id uuid,
  email text,
  ip inet,
  user_agent text,
  result text, -- 'success', 'failed', 'blocked'
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_loginlog_user ON app.login_log (user_id, created_at DESC);
CREATE INDEX idx_loginlog_tenant ON app.login_log (tenant_id, created_at DESC);

-- === 10.3: Auth Audit - Track token and authentication lifecycle events ===
CREATE TABLE IF NOT EXISTS app.auth_audit (
  id bigserial PRIMARY KEY,
  tenant_id uuid,
  user_id uuid,
  actor_user_id uuid,
  action text NOT NULL, -- 'refresh_issued','refresh_revoked','jti_revoked','logout'
  resource_type text,
  resource_id uuid,
  payload jsonb,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_auth_audit_tenant ON app.auth_audit(tenant_id, created_at DESC);
CREATE INDEX idx_auth_audit_user ON app.auth_audit(user_id, created_at DESC);

-- === 10.4: Feature Flags - Enable/disable features per tenant or globally ===
CREATE TABLE app.feature_flags (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid REFERENCES app.tenants(id) ON DELETE CASCADE, -- NULL => global flag
  key text NOT NULL,
  value jsonb NOT NULL,
  enabled boolean DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_flags_tenant_key ON app.feature_flags(tenant_id, lower(key));

-- === 10.5: Application Configuration ===
CREATE TABLE IF NOT EXISTS app.app_config (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  config_key text NOT NULL UNIQUE,
  config_value text NOT NULL,
  description text,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_app_config_key ON app.app_config(lower(config_key));
CREATE INDEX idx_app_config_created_at ON app.app_config(created_at DESC);

-- ============================================================================
-- PHASE 12: AUDIT TRIGGERS & INDEXES
-- ============================================================================

-- Creates audit triggers on all tables that have audit columns
DO $$
DECLARE
  tbl text;
BEGIN
  FOR tbl IN SELECT tablename FROM pg_tables WHERE schemaname = 'app' AND tablename NOT LIKE 'pg_%' LOOP
    EXECUTE format('
      CREATE TRIGGER IF NOT EXISTS trg_%1$I_audit
      BEFORE INSERT OR UPDATE ON app.%1$I
      FOR EACH ROW EXECUTE FUNCTION app.set_audit_fields();', tbl);
  END LOOP;
END;
$$;

-- ============================================================================
-- PHASE 13: PERFORMANCE INDEXES
-- ============================================================================

-- Tenant & Subscription indexes
CREATE INDEX IF NOT EXISTS idx_tenant_subscriptions_tenant ON app.tenant_subscriptions(tenant_id);
ALTER TABLE app.tenant_subscriptions ADD CONSTRAINT fk_tenant_subscriptions_user_created 
  FOREIGN KEY (created_by) REFERENCES app.users(id) ON DELETE SET NULL;
ALTER TABLE app.tenant_subscriptions ADD CONSTRAINT fk_tenant_subscriptions_user_updated 
  FOREIGN KEY (updated_by) REFERENCES app.users(id) ON DELETE SET NULL;

-- User & Authentication indexes
CREATE INDEX IF NOT EXISTS idx_users_tenant ON app.users(tenant_id);
CREATE INDEX IF NOT EXISTS idx_roles_tenant ON app.roles(tenant_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON app.user_roles(role_id);

-- Member indexes
CREATE INDEX IF NOT EXISTS idx_members_status ON app.members(status);
CREATE INDEX IF NOT EXISTS idx_members_user ON app.members(user_id);

-- Product & Inventory indexes
CREATE INDEX IF NOT EXISTS idx_products_category ON app.products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_active ON app.products(is_active);
CREATE INDEX IF NOT EXISTS idx_inventories_product ON app.inventories(product_id);
CREATE INDEX IF NOT EXISTS idx_inventories_location ON app.inventories(location);

-- Supplier indexes
CREATE INDEX IF NOT EXISTS idx_suppliers_tenant ON app.suppliers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_supplier_prices_supplier ON app.supplier_product_prices(supplier_id);

-- Order & Payment indexes
CREATE INDEX IF NOT EXISTS idx_orders_status ON app.orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_buyer ON app.orders(buyer_id);
CREATE INDEX IF NOT EXISTS idx_orders_supplier ON app.orders(supplier_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product ON app.order_items(product_id);
CREATE INDEX IF NOT EXISTS idx_payments_order ON app.payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_tenant ON app.payments(tenant_id);

-- Financial indexes
CREATE INDEX IF NOT EXISTS idx_savings_member ON app.savings_accounts(member_id);
CREATE INDEX IF NOT EXISTS idx_loans_member ON app.loans(member_id);

-- Document indexes
CREATE INDEX IF NOT EXISTS idx_documents_supplier ON app.documents(supplier_id);
CREATE INDEX IF NOT EXISTS idx_documents_status ON app.documents(status);

-- JSONB indexes
CREATE INDEX IF NOT EXISTS idx_tenants_metadata_gin ON app.tenants USING gin (metadata);
CREATE INDEX IF NOT EXISTS idx_members_metadata_gin ON app.members USING gin (metadata);
CREATE INDEX IF NOT EXISTS idx_suppliers_metadata_gin ON app.suppliers USING gin (metadata);
CREATE INDEX IF NOT EXISTS idx_orders_metadata_gin ON app.orders USING gin (metadata);