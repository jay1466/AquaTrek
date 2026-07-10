-- ============================================================
-- AquaTrack V2 — Authentication & User Management Schema
-- ============================================================
-- Creates:
--   1. roles                       — system roles (ADMIN, RESIDENT…)
--   2. users                       — registered users across all tenants
--   3. user_login_attempts         — brute-force protection tracking
--   4. refresh_tokens              — JWT refresh token store
--   5. email_verification_tokens   — email verification links
--   6. password_reset_tokens       — password reset links
--   7. token_blacklist             — revoked access tokens (logout)
-- ============================================================
-- Author  : AquaTrack Engineering Team
-- Version : 1.0.0
-- ============================================================

-- ── 1. roles ─────────────────────────────────────────────────
-- Seeded at startup; not user-modifiable via the API.

CREATE TABLE IF NOT EXISTS roles (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    name            VARCHAR(50)     NOT NULL,   -- Matches UserRole enum: SUPER_ADMIN | ADMIN | MANAGER | RESIDENT
    display_name    VARCHAR(100)    NOT NULL,
    description     VARCHAR(500),

    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255)    NOT NULL DEFAULT 'SYSTEM',
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name),
    CONSTRAINT chk_roles_name CHECK (name IN ('SUPER_ADMIN','ADMIN','MANAGER','RESIDENT')),
    CONSTRAINT chk_roles_status CHECK (status IN ('ACTIVE','INACTIVE','DELETED'))
);


-- ── 2. users ─────────────────────────────────────────────────
-- Multi-tenant: apartment_id links the user to their society.
-- A user belongs to exactly one apartment society.

CREATE TABLE IF NOT EXISTS users (
    id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    apartment_id            UUID,           -- NULL only for SUPER_ADMIN (cross-tenant)
    role_id                 UUID            NOT NULL,
    household_id            UUID,           -- Set when a RESIDENT is assigned to a household

    -- Identity
    email                   VARCHAR(255)    NOT NULL,
    password_hash           VARCHAR(255)    NOT NULL,
    first_name              VARCHAR(100)    NOT NULL,
    last_name               VARCHAR(100)    NOT NULL,
    phone_number            VARCHAR(20),
    gender                  VARCHAR(30),
    profile_photo_url       VARCHAR(1000),

    -- Account lifecycle
    email_verified          BOOLEAN         NOT NULL DEFAULT FALSE,
    account_locked          BOOLEAN         NOT NULL DEFAULT FALSE,
    account_locked_until    TIMESTAMP,
    failed_login_attempts   INTEGER         NOT NULL DEFAULT 0,
    last_login_at           TIMESTAMP,
    last_login_ip           VARCHAR(45),
    password_changed_at     TIMESTAMP,

    -- Standard audit columns
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP,
    created_by              VARCHAR(255)    NOT NULL DEFAULT 'SYSTEM',
    updated_by              VARCHAR(255),
    version                 BIGINT          NOT NULL DEFAULT 0,
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    status                  VARCHAR(50)     NOT NULL DEFAULT 'PENDING',  -- PENDING until email verified

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED','PENDING','DELETED')),
    CONSTRAINT chk_users_gender CHECK (gender IN ('MALE','FEMALE','OTHER','PREFER_NOT_TO_SAY') OR gender IS NULL)
);

-- Tenant-scoped queries (most common access pattern)
CREATE INDEX IF NOT EXISTS idx_users_apartment_id
    ON users (apartment_id)
    WHERE is_deleted = FALSE;

-- Email lookup (login)
CREATE INDEX IF NOT EXISTS idx_users_email
    ON users (email)
    WHERE is_deleted = FALSE;

-- Household member lookup
CREATE INDEX IF NOT EXISTS idx_users_household_id
    ON users (household_id)
    WHERE is_deleted = FALSE;

-- Role-based filtering
CREATE INDEX IF NOT EXISTS idx_users_role_id
    ON users (role_id, apartment_id);


-- ── 3. user_login_attempts ───────────────────────────────────
-- Fine-grained login attempt log for audit and anomaly detection.

CREATE TABLE IF NOT EXISTS user_login_attempts (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    email           VARCHAR(255)    NOT NULL,
    ip_address      VARCHAR(45),
    user_agent      TEXT,
    success         BOOLEAN         NOT NULL DEFAULT FALSE,
    failure_reason  VARCHAR(255),
    attempted_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Minimal columns (append-only log; no soft-delete needed)
    version         BIGINT          NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255)    NOT NULL DEFAULT 'SYSTEM',
    updated_by      VARCHAR(255),

    CONSTRAINT pk_user_login_attempts PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_login_attempts_email
    ON user_login_attempts (email, attempted_at DESC);

CREATE INDEX IF NOT EXISTS idx_login_attempts_ip
    ON user_login_attempts (ip_address, attempted_at DESC);


-- ── 4. refresh_tokens ────────────────────────────────────────
-- Stores active refresh tokens. Rotated on each use.

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL,
    apartment_id    UUID,
    token_hash      VARCHAR(500)    NOT NULL,   -- SHA-256 hash of the raw token
    expires_at      TIMESTAMP       NOT NULL,
    revoked         BOOLEAN         NOT NULL DEFAULT FALSE,
    revoked_at      TIMESTAMP,
    revoked_reason  VARCHAR(255),               -- e.g. "LOGOUT" | "ROTATION" | "SECURITY"
    issued_ip       VARCHAR(45),
    user_agent      TEXT,

    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255)    NOT NULL DEFAULT 'SYSTEM',
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user
    ON refresh_tokens (user_id, revoked, expires_at);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_hash
    ON refresh_tokens (token_hash)
    WHERE revoked = FALSE;


-- ── 5. email_verification_tokens ─────────────────────────────

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL,
    token           VARCHAR(255)    NOT NULL,
    expires_at      TIMESTAMP       NOT NULL,
    used            BOOLEAN         NOT NULL DEFAULT FALSE,
    used_at         TIMESTAMP,

    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255)    NOT NULL DEFAULT 'SYSTEM',
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT pk_email_verification_tokens PRIMARY KEY (id),
    CONSTRAINT uq_email_verification_token UNIQUE (token),
    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_email_verification_token
    ON email_verification_tokens (token)
    WHERE used = FALSE AND is_deleted = FALSE;


-- ── 6. password_reset_tokens ─────────────────────────────────

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL,
    token           VARCHAR(255)    NOT NULL,
    expires_at      TIMESTAMP       NOT NULL,
    used            BOOLEAN         NOT NULL DEFAULT FALSE,
    used_at         TIMESTAMP,
    requested_ip    VARCHAR(45),

    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255)    NOT NULL DEFAULT 'SYSTEM',
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT uq_password_reset_token UNIQUE (token),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_password_reset_token
    ON password_reset_tokens (token)
    WHERE used = FALSE AND is_deleted = FALSE;


-- ── 7. token_blacklist ────────────────────────────────────────
-- Stores revoked ACCESS tokens (JWT) until their natural expiry.
-- Checked by JwtAuthFilter to block use of logged-out tokens.

CREATE TABLE IF NOT EXISTS token_blacklist (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    token_jti       VARCHAR(255)    NOT NULL,   -- JWT "jti" (unique token ID) claim
    user_id         UUID,
    revoked_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP       NOT NULL,   -- Copied from JWT exp — allows scheduled cleanup
    reason          VARCHAR(255),               -- e.g. "LOGOUT" | "PASSWORD_CHANGE" | "ADMIN_REVOKE"

    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255)    NOT NULL DEFAULT 'SYSTEM',
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT pk_token_blacklist PRIMARY KEY (id),
    CONSTRAINT uq_token_blacklist_jti UNIQUE (token_jti)
);

-- High-frequency lookup on every authenticated request
CREATE INDEX IF NOT EXISTS idx_token_blacklist_jti
    ON token_blacklist (token_jti);

-- For the scheduled cleanup job (removes expired entries)
CREATE INDEX IF NOT EXISTS idx_token_blacklist_expires
    ON token_blacklist (expires_at);


-- ── 8. Seed: Default Roles ────────────────────────────────────

INSERT INTO roles (name, display_name, description, created_by) VALUES
('SUPER_ADMIN', 'Super Administrator', 'Platform-level administrator with cross-tenant access. Assigned only via direct DB operations.', 'SYSTEM'),
('ADMIN',       'Administrator',       'Apartment society administrator. Full access within their apartment society.',                  'SYSTEM'),
('MANAGER',     'Manager',             'Building/block manager. Can manage households and meters within assigned buildings.',           'SYSTEM'),
('RESIDENT',    'Resident',            'Household resident. Read-only access to own household data, invoices and usage.',               'SYSTEM')
ON CONFLICT (name) DO NOTHING;
