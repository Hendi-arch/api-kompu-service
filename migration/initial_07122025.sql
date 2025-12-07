-- 2025-12-07_koperasi_multitenant_schema.sql
-- Use with Flyway or Liquibase. Each CREATE can be separated into migrations.

-- === Extensions ===
CREATE EXTENSION IF NOT EXISTS "pgcrypto";    -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pg_trgm";     -- for text search indexing if required

-- === Schemas ===
CREATE SCHEMA IF NOT EXISTS app AUTHORIZATION postgres;

SET search_path = app, public;

-- === Utility functions for session-based tenant/actor context ===
-- Applications (Spring Boot) must set these on connection: 
-- SELECT set_config('app.current_tenant', '<tenant_uuid>', true);
-- SELECT set_config('app.current_user_id', '<user_uuid>', true);
-- SELECT set_config('app.current_roles', 'role1,role2', true);
-- Optionally: set_config('app.is_super_admin','true', true);

CREATE OR REPLACE FUNCTION app.current_tenant() RETURNS uuid AS $$
  SELECT (current_setting('app.current_tenant', true))::uuid;
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION app.current_user_id() RETURNS uuid AS $$
  SELECT (current_setting('app.current_user_id', true))::uuid;
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION app.is_super_admin() RETURNS boolean AS $$
  SELECT (current_setting('app.is_super_admin', true) = 'true');
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION app.current_roles() RETURNS text AS $$
  SELECT current_setting('app.current_roles', true);
$$ LANGUAGE sql STABLE;

-- Helper: check whether current session has a role
CREATE OR REPLACE FUNCTION app.session_has_role(role_name text) RETURNS boolean AS $$
  SELECT position(lower(role_name) in coalesce(lower(current_setting('app.current_roles', true)), '')) > 0;
$$ LANGUAGE sql STABLE;

-- === Audit trigger to fill created_at/updated_at/created_by/updated_by ===
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

-- Generic audit columns comment:
-- created_at timestamptz NOT NULL DEFAULT now()
-- updated_at timestamptz NOT NULL DEFAULT now()
-- created_by uuid NULL
-- updated_by uuid NULL
-- deleted_at timestamptz NULL -- soft-delete flag

-- === Core: Tenants & Domains ===
CREATE TABLE app.tenants (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name text NOT NULL,
  code text UNIQUE, -- short code
  status text NOT NULL DEFAULT 'active', -- active | suspended | archived
  metadata jsonb DEFAULT '{}'::jsonb,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by uuid,
  updated_by uuid,
  deleted_at timestamptz
);

CREATE INDEX IF NOT EXISTS idx_tenants_status ON app.tenants(status);

CREATE TABLE app.tenant_domains (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid REFERENCES app.tenants(id) ON DELETE CASCADE,
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

-- === RBAC: roles, permissions, role_permissions, user_roles ===
CREATE TABLE app.roles (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid,            -- NULL => global role (super-admin roles)
  name text NOT NULL,
  description text,
  is_system boolean DEFAULT false,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_roles_tenant_name ON app.roles(tenant_id, lower(name));

CREATE TABLE app.permissions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  code text NOT NULL UNIQUE,   -- e.g. products.create, orders.read
  description text,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE app.role_permissions (
  role_id uuid REFERENCES app.roles(id) ON DELETE CASCADE,
  permission_id uuid REFERENCES app.permissions(id) ON DELETE CASCADE,
  PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE app.users (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid,                   -- NULL => global (super admin) account
  username text NOT NULL,
  email text NOT NULL,
  password_hash text,               -- managed by app; nullable for SSO users
  full_name text,
  phone text,
  avatar_url text,
  is_active boolean DEFAULT true,
  is_email_verified boolean DEFAULT false,
  is_system boolean DEFAULT false,  -- internal system accounts
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by uuid,
  updated_by uuid,
  deleted_at timestamptz
);
CREATE UNIQUE INDEX idx_users_email_tenant ON app.users (tenant_id, lower(email));
CREATE UNIQUE INDEX idx_users_username_tenant ON app.users (tenant_id, lower(username));

CREATE TABLE app.user_roles (
  user_id uuid REFERENCES app.users(id) ON DELETE CASCADE,
  role_id uuid REFERENCES app.roles(id) ON DELETE CASCADE,
  assigned_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, role_id)
);

-- === Members / Employees (tenant-scoped) ===
CREATE TABLE app.members (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  member_code text,
  user_id uuid REFERENCES app.users(id),
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

-- === Product Catalog / Inventory ===
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

-- === Orders / Payments / Transactions ===
CREATE TABLE app.orders (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES app.tenants(id) ON DELETE CASCADE,
  order_number text NOT NULL,
  buyer_id uuid REFERENCES app.members(id) ON DELETE SET NULL,
  buyer_snapshot jsonb,  -- store denormalized buyer details
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

-- === Simple Loan & Saving placeholders (koperasi financial basics) ===
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

-- === System: configurations, feature flags, audit logs ===
CREATE TABLE app.feature_flags (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid, -- NULL => global flag
  key text NOT NULL,
  value jsonb NOT NULL,
  enabled boolean DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_flags_tenant_key ON app.feature_flags(tenant_id, lower(key));

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

CREATE TABLE app.login_log (
  id bigserial PRIMARY KEY,
  tenant_id uuid,
  user_id uuid,
  username text,
  ip inet,
  user_agent text,
  result text,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_loginlog_user ON app.login_log (user_id, created_at DESC);

-- === Search / Full text helper columns (optional) ===
-- Add GIN indexes on JSONB fields if necessary:
CREATE INDEX idx_products_metadata_gin ON app.products USING gin (metadata);
CREATE INDEX idx_members_metadata_gin ON app.members USING gin (metadata);

-- Security entities DDL (append to your migrations)
-- Requires: CREATE EXTENSION pgcrypto; (already created)

-- === Tracks login sessions (device + IP) for visibility and auditing
CREATE TABLE IF NOT EXISTS app.user_sessions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid,                                   -- NULL for super admin
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

-- === Stores hashed refresh tokens, supports revocation + rotation
CREATE TABLE IF NOT EXISTS app.refresh_tokens (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES app.users(id) ON DELETE CASCADE,
  session_id uuid REFERENCES app.user_sessions(id) ON DELETE SET NULL,
  token_hash bytea NOT NULL,                       -- store hash, not plaintext
  created_at timestamptz NOT NULL DEFAULT now(),
  expires_at timestamptz NOT NULL,
  revoked_at timestamptz,
  CONSTRAINT ux_refresh_user_hash UNIQUE (user_id, token_hash)
);

CREATE INDEX idx_refresh_tokens_user ON app.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash ON app.refresh_tokens(token_hash);

-- === Blacklist for revoked access tokens / forced logout.
CREATE TABLE IF NOT EXISTS app.revoked_jtis (
  jti uuid PRIMARY KEY,
  user_id uuid REFERENCES app.users(id) ON DELETE SET NULL,
  revoked_at timestamptz NOT NULL DEFAULT now(),
  expires_at timestamptz NOT NULL
);

CREATE INDEX idx_revoked_jtis_user ON app.revoked_jtis(user_id);

-- === auth_audit: audit trail for token/revoke actions (optional)
CREATE TABLE IF NOT EXISTS app.auth_audit (
  id bigserial PRIMARY KEY,
  tenant_id uuid,
  user_id uuid,
  actor_user_id uuid,
  action text NOT NULL,         -- 'refresh_issued','refresh_revoked','jti_revoked','logout'
  resource_type text,
  resource_id uuid,
  payload jsonb,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_auth_audit_tenant ON app.auth_audit(tenant_id, created_at DESC);

-- === Audit triggers applied to tenant-scoped tables ===
DO $$
DECLARE
  tbl text;
BEGIN
  FOR tbl IN SELECT tablename FROM pg_tables WHERE schemaname = 'app' AND tablename NOT LIKE 'pg_%' LOOP
    EXECUTE format('
      CREATE TRIGGER trg_%1$I_audit
      BEFORE INSERT OR UPDATE ON app.%1$I
      FOR EACH ROW EXECUTE FUNCTION app.set_audit_fields();', tbl);
  END LOOP;
END;
$$;

-- === Row-Level Security (RLS) ===
-- Apply RLS to tenant-scoped tables (explicit list)
-- Helper: tables that must be tenant-isolated:
-- tenants (no RLS, super-admin only), tenant_domains (tenant scoped), roles (tenant-scoped), users (tenant-scoped),
-- members, product_categories, products, inventories, orders (and partitions), order_items, payments, savings_accounts, loans, feature_flags

-- For each tenant-scoped table, enable RLS and create two policies:
--  1) Read/Select: allow if tenant_id matches session tenant OR session is super-admin
--  2) Write/Insert/Update/Delete: same check plus check on tenant_id in WITH CHECK for inserts/updates

CREATE OR REPLACE FUNCTION app.rls_allow_tenant(tenant_col text) RETURNS text AS $$
  SELECT format('(%s IS NULL) OR (%s = app.current_tenant()) OR app.is_super_admin()', tenant_col, tenant_col);
$$ LANGUAGE sql;

-- Utility: a small list of tenant-scoped tables to apply RLS to:
-- We'll create a script to enable RLS for each table.

-- Example: enable RLS on products
ALTER TABLE app.products ENABLE ROW LEVEL SECURITY;

CREATE POLICY products_select_policy ON app.products
  FOR SELECT USING ( (tenant_id IS NULL) OR (tenant_id = app.current_tenant()) OR app.is_super_admin() );

CREATE POLICY products_write_policy ON app.products
  FOR INSERT, UPDATE, DELETE
  USING ( (tenant_id IS NULL) OR (tenant_id = app.current_tenant()) OR app.is_super_admin() )
  WITH CHECK ( (tenant_id IS NULL) OR (tenant_id = app.current_tenant()) OR app.is_super_admin() );

-- Apply same approach to other tenant-scoped tables:
-- members
ALTER TABLE app.members ENABLE ROW LEVEL SECURITY;
CREATE POLICY members_select_policy ON app.members
  FOR SELECT USING ( (tenant_id = app.current_tenant()) OR app.is_super_admin() );
CREATE POLICY members_write_policy ON app.members
  FOR INSERT, UPDATE, DELETE
  USING ( (tenant_id = app.current_tenant()) OR app.is_super_admin() )
  WITH CHECK ( (tenant_id = app.current_tenant()) OR app.is_super_admin() );

-- inventories
ALTER TABLE app.inventories ENABLE ROW LEVEL SECURITY;
CREATE POLICY inventories_select_policy ON app.inventories
  FOR SELECT USING ( (tenant_id = app.current_tenant()) OR app.is_super_admin() );
CREATE POLICY inventories_write_policy ON app.inventories
  FOR INSERT, UPDATE, DELETE
  USING ( (tenant_id = app.current_tenant()) OR app.is_super_admin() )
  WITH CHECK ( (tenant_id = app.current_tenant()) OR app.is_super_admin() );

-- orders & partitions
ALTER TABLE app.orders ENABLE ROW LEVEL SECURITY;
CREATE POLICY orders_select_policy ON app.orders
  FOR SELECT USING ( (tenant_id = app.current_tenant()) OR app.is_super_admin() );
CREATE POLICY orders_write_policy ON app.orders
  FOR INSERT, UPDATE, DELETE
  USING ( (tenant_id = app.current_tenant()) OR app.is_super_admin() )
  WITH CHECK ( (tenant_id = app.current_tenant()) OR app.is_super_admin() );

-- payments
ALTER TABLE app.payments ENABLE ROW LEVEL SECURITY;
CREATE POLICY payments_select_policy ON app.payments
  FOR SELECT USING ( (tenant_id = app.current_tenant()) OR app.is_super_admin() );
CREATE POLICY payments_write_policy ON app.payments
  FOR INSERT, UPDATE, DELETE
  USING ( (tenant_id = app.current_tenant()) OR app.is_super_admin() )
  WITH CHECK ( (tenant_id = app.current_tenant()) OR app.is_super_admin() );

-- product_categories
ALTER TABLE app.product_categories ENABLE ROW LEVEL SECURITY;
CREATE POLICY product_categories_select_policy ON app.product_categories
  FOR SELECT USING ( (tenant_id = app.current_tenant()) OR app.is_super_admin() );
CREATE POLICY product_categories_write_policy ON app.product_categories
  FOR INSERT, UPDATE, DELETE
  USING ( (tenant_id = app.current_tenant()) OR app.is_super_admin() )
  WITH CHECK ( (tenant_id = app.current_tenant()) OR app.is_super_admin() );

-- roles (tenant or global)
ALTER TABLE app.roles ENABLE ROW LEVEL SECURITY;
CREATE POLICY roles_select_policy ON app.roles
  FOR SELECT USING ( (tenant_id IS NULL) OR (tenant_id = app.current_tenant()) OR app.is_super_admin() );
CREATE POLICY roles_write_policy ON app.roles
  FOR INSERT, UPDATE, DELETE
  USING ( (tenant_id IS NULL) OR (tenant_id = app.current_tenant()) OR app.is_super_admin() )
  WITH CHECK ( (tenant_id IS NULL) OR (tenant_id = app.current_tenant()) OR app.is_super_admin() );

-- users: allow global (NULL tenant) and tenant members
ALTER TABLE app.users ENABLE ROW LEVEL SECURITY;
CREATE POLICY users_select_policy ON app.users
  FOR SELECT USING ( (tenant_id IS NULL) OR (tenant_id = app.current_tenant()) OR app.is_super_admin() );
CREATE POLICY users_write_policy ON app.users
  FOR INSERT, UPDATE, DELETE
  USING ( (tenant_id IS NULL) OR (tenant_id = app.current_tenant()) OR app.is_super_admin() )
  WITH CHECK ( (tenant_id IS NULL) OR (tenant_id = app.current_tenant()) OR app.is_super_admin() );

-- savings_accounts, loans
ALTER TABLE app.savings_accounts ENABLE ROW LEVEL SECURITY;
CREATE POLICY savings_select_policy ON app.savings_accounts
  FOR SELECT USING ( tenant_id = app.current_tenant() OR app.is_super_admin() );
CREATE POLICY savings_write_policy ON app.savings_accounts
  FOR INSERT, UPDATE, DELETE
  USING ( tenant_id = app.current_tenant() OR app.is_super_admin() )
  WITH CHECK ( tenant_id = app.current_tenant() OR app.is_super_admin() );

ALTER TABLE app.loans ENABLE ROW LEVEL SECURITY;
CREATE POLICY loans_select_policy ON app.loans
  FOR SELECT USING ( tenant_id = app.current_tenant() OR app.is_super_admin() );
CREATE POLICY loans_write_policy ON app.loans
  FOR INSERT, UPDATE, DELETE
  USING ( tenant_id = app.current_tenant() OR app.is_super_admin() )
  WITH CHECK ( tenant_id = app.current_tenant() OR app.is_super_admin() );

-- domain table: tenant_id may be null for global-managed domains (super admin)
ALTER TABLE app.tenant_domains ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_domains_select_policy ON app.tenant_domains
  FOR SELECT USING ( tenant_id IS NULL OR tenant_id = app.current_tenant() OR app.is_super_admin() );
CREATE POLICY tenant_domains_write_policy ON app.tenant_domains
  FOR INSERT, UPDATE, DELETE
  USING ( tenant_id IS NULL OR tenant_id = app.current_tenant() OR app.is_super_admin() )
  WITH CHECK ( tenant_id IS NULL OR tenant_id = app.current_tenant() OR app.is_super_admin() );

-- activity_log and login_log: allow tenant scope or super-admin
ALTER TABLE app.activity_log ENABLE ROW LEVEL SECURITY;
CREATE POLICY activity_log_select_policy ON app.activity_log
  FOR SELECT USING ( tenant_id IS NULL OR tenant_id = app.current_tenant() OR app.is_super_admin() );
CREATE POLICY login_log_select_policy ON app.login_log
  FOR SELECT USING ( tenant_id IS NULL OR tenant_id = app.current_tenant() OR app.is_super_admin() );

-- === Indexes for performance (examples) ===
CREATE INDEX IF NOT EXISTS idx_orders_tenant_created_at ON app.orders (tenant_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_order_items_orderid ON app.order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_products_tenant_price ON app.products (tenant_id, price);
CREATE INDEX IF NOT EXISTS idx_inventories_tenant_product ON app.inventories (tenant_id, product_id);

-- === Views (optional): tenant-scoped safe views for reporting (enforce RLS) ===
CREATE OR REPLACE VIEW app.v_tenant_products AS
SELECT id, tenant_id, sku, name, price, is_active FROM app.products;
-- RLS on underlying table will enforce tenant isolation on view

-- === Example helper function to set tenant from app (used by migrations/tools) ===
-- SELECT set_config('app.current_tenant', '00000000-0000-0000-0000-000000000000', true);

-- END OF DDL
