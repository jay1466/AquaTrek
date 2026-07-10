-- ============================================================
-- AquaTrack V1 — Initial Schema Foundation
-- ============================================================
-- Sets up:
--   1. PostgreSQL extensions required by the application
--   2. system_configurations  — application key/value config store
--   3. audit_logs             — entity-level change tracking
--   4. activity_logs          — user action history
-- ============================================================
-- Author  : AquaTrack Engineering Team
-- Version : 1.0.0
-- ============================================================

-- ── 1. PostgreSQL Extensions ─────────────────────────────────

-- UUID generation: gen_random_uuid() used by all entity PKs
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Full-text search support (used in analytics and search features)
CREATE EXTENSION IF NOT EXISTS "pg_trgm";


-- ── 2. system_configurations ─────────────────────────────────
-- Key-value store for application-wide configuration that admins
-- can modify without redeployment.
-- Scoped at the GLOBAL level (applies to all apartment societies).

CREATE TABLE IF NOT EXISTS system_configurations (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    config_key      VARCHAR(255)    NOT NULL,
    config_value    TEXT,
    description     VARCHAR(1000),
    data_type       VARCHAR(50)     NOT NULL DEFAULT 'STRING',  -- STRING | INTEGER | BOOLEAN | JSON
    is_encrypted    BOOLEAN         NOT NULL DEFAULT FALSE,
    is_editable     BOOLEAN         NOT NULL DEFAULT TRUE,       -- FALSE for internal-only configs

    -- Standard audit columns (present on every table)
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255)    NOT NULL DEFAULT 'SYSTEM',
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT pk_system_configurations PRIMARY KEY (id),
    CONSTRAINT uq_system_config_key UNIQUE (config_key),
    CONSTRAINT chk_system_config_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED'))
);

-- Partial index: only active, non-deleted configs are typically queried
CREATE INDEX IF NOT EXISTS idx_system_config_key_active
    ON system_configurations (config_key)
    WHERE is_deleted = FALSE AND status = 'ACTIVE';


-- ── 3. audit_logs ────────────────────────────────────────────
-- Records every create/update/delete action on domain entities.
-- Multi-tenant: scoped by apartment_id (NULL for platform-level actions).

CREATE TABLE IF NOT EXISTS audit_logs (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    apartment_id    UUID,                                       -- NULL for super-admin/system actions
    entity_name     VARCHAR(255)    NOT NULL,                   -- e.g. "Apartment", "Invoice"
    entity_id       UUID,                                       -- PK of the modified entity
    action          VARCHAR(100)    NOT NULL,                   -- CREATE | UPDATE | DELETE | VIEW
    old_value       TEXT,                                       -- JSON snapshot before change
    new_value       TEXT,                                       -- JSON snapshot after change
    changed_fields  TEXT,                                       -- JSON array of changed field names
    performed_by    VARCHAR(255),                               -- User email or "SYSTEM"
    ip_address      VARCHAR(45),                                -- IPv4 or IPv6
    user_agent      TEXT,

    -- Audit logs are append-only — no update/delete columns needed
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255)    NOT NULL DEFAULT 'SYSTEM',

    -- Minimal compliance columns
    version         BIGINT          NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);

-- Query pattern: find all audit entries for a specific entity
CREATE INDEX IF NOT EXISTS idx_audit_entity
    ON audit_logs (entity_name, entity_id);

-- Query pattern: find all actions by a specific user within a tenant
CREATE INDEX IF NOT EXISTS idx_audit_apartment_user
    ON audit_logs (apartment_id, performed_by);

-- Query pattern: recent activity feed
CREATE INDEX IF NOT EXISTS idx_audit_created_at
    ON audit_logs (created_at DESC);


-- ── 4. activity_logs ─────────────────────────────────────────
-- User-facing activity timeline (e.g. "Admin generated invoice INV-202401-X").
-- Designed for the dashboard activity feed.

CREATE TABLE IF NOT EXISTS activity_logs (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    apartment_id    UUID            NOT NULL,
    user_id         UUID,                                       -- The user who performed the action
    action_type     VARCHAR(100)    NOT NULL,                   -- e.g. INVOICE_GENERATED, READING_UPLOADED
    description     VARCHAR(1000)   NOT NULL,                   -- Human-readable action summary
    entity_name     VARCHAR(255),
    entity_id       UUID,
    metadata        TEXT,                                       -- Optional JSON for additional context

    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255)    NOT NULL DEFAULT 'SYSTEM',
    version         BIGINT          NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT pk_activity_logs PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_activity_apartment_time
    ON activity_logs (apartment_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_activity_user
    ON activity_logs (user_id, created_at DESC);


-- ── 5. Seed: Default system_configurations ───────────────────
-- These defaults are applied on fresh installation.
-- Admins can override values through the Settings API.

INSERT INTO system_configurations (config_key, config_value, description, data_type, is_editable) VALUES
('APP_VERSION',                     '1.0.0',    'Current application version',                              'STRING',   FALSE),
('MAINTENANCE_MODE',                'false',     'Put application in maintenance mode (true/false)',         'BOOLEAN',  TRUE),
('MAX_FAILED_LOGIN_ATTEMPTS',       '5',         'Max failed logins before account lock',                   'INTEGER',  TRUE),
('ACCOUNT_LOCK_DURATION_MINUTES',   '30',        'Duration (minutes) account stays locked after max fails', 'INTEGER',  TRUE),
('OTP_EXPIRY_MINUTES',              '15',        'OTP validity duration in minutes',                        'INTEGER',  TRUE),
('PASSWORD_RESET_EXPIRY_HOURS',     '24',        'Password reset link validity in hours',                   'INTEGER',  TRUE),
('EMAIL_VERIFICATION_EXPIRY_HOURS', '48',        'Email verification link validity in hours',               'INTEGER',  TRUE),
('DEFAULT_PAGE_SIZE',               '20',        'Default pagination page size',                            'INTEGER',  TRUE),
('MAX_PAGE_SIZE',                   '100',       'Maximum allowed pagination page size',                    'INTEGER',  TRUE),
('DEFAULT_LATE_FEE_PERCENTAGE',     '2.0',       'Default late payment fee percentage',                     'STRING',   TRUE),
('LATE_FEE_GRACE_DAYS',             '7',         'Grace period (days) before late fee is applied',          'INTEGER',  TRUE),
('INVOICE_DUE_DAYS',                '15',        'Days from invoice generation until payment due',          'INTEGER',  TRUE),
('MAX_READING_INCREASE_PERCENT',    '500',       'Max % reading increase before flagging as suspicious',    'INTEGER',  TRUE),
('LEAK_THRESHOLD_KL_PER_DAY',       '0.5',       'Min daily KL flow that triggers a leak alert',           'STRING',   TRUE),
('ALLOW_SELF_READING',              'false',     'Allow residents to submit their own meter readings',      'BOOLEAN',  TRUE),
('WATER_UNIT',                      'KL',        'Unit of water measurement (KL = Kilolitres)',             'STRING',   FALSE),
('CURRENCY_SYMBOL',                 '₹',         'Currency symbol used in invoices',                        'STRING',   TRUE),
('CURRENCY_CODE',                   'INR',       'ISO currency code',                                       'STRING',   TRUE),
('SUPPORT_EMAIL',                   'support@aquatrack.com', 'Support contact email shown in invoices',    'STRING',   TRUE),
('INVOICE_FOOTER_TEXT',             'Thank you for your timely payment.',
                                                 'Footer text displayed on all PDF invoices',              'STRING',   TRUE)
ON CONFLICT (config_key) DO NOTHING;
