-- ============================================================
-- AquaTrack V3 — Apartment & Building Schema
-- ============================================================
-- Creates:
--   1. apartments          — the top-level tenant entity
--   2. buildings           — blocks/wings within an apartment society
--   3. apartment_settings  — per-tenant configurable key-value settings
-- ============================================================

-- ── 1. apartments ─────────────────────────────────────────────
-- Each apartment society is a fully isolated tenant.
-- Every other entity references apartment_id for multi-tenancy.

CREATE TABLE IF NOT EXISTS apartments (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    name                VARCHAR(255)    NOT NULL,
    registration_number VARCHAR(100),
    address_line1       VARCHAR(255)    NOT NULL,
    address_line2       VARCHAR(255),
    city                VARCHAR(100)    NOT NULL,
    state               VARCHAR(100)    NOT NULL,
    pincode             VARCHAR(20)     NOT NULL,
    country             VARCHAR(100)    NOT NULL DEFAULT 'India',
    contact_email       VARCHAR(255),
    contact_phone       VARCHAR(20),
    website_url         VARCHAR(500),
    logo_url            VARCHAR(1000),
    total_units         INTEGER         NOT NULL DEFAULT 0,
    total_buildings     INTEGER         NOT NULL DEFAULT 0,
    established_year    INTEGER,
    subscription_plan   VARCHAR(50)     NOT NULL DEFAULT 'BASIC',
    subscription_valid_until TIMESTAMP,

    -- Standard audit columns
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255)    NOT NULL DEFAULT 'SYSTEM',
    updated_by          VARCHAR(255),
    version             BIGINT          NOT NULL DEFAULT 0,
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    status              VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT pk_apartments PRIMARY KEY (id),
    CONSTRAINT uq_apartments_name UNIQUE (name),
    CONSTRAINT chk_apartments_status CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED','PENDING','DELETED')),
    CONSTRAINT chk_apartments_plan CHECK (subscription_plan IN ('BASIC','STANDARD','PREMIUM','ENTERPRISE'))
);

CREATE INDEX IF NOT EXISTS idx_apartments_status
    ON apartments (status) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_apartments_city
    ON apartments (city, status) WHERE is_deleted = FALSE;


-- ── 2. buildings ──────────────────────────────────────────────
-- A building/block/wing within an apartment society.

CREATE TABLE IF NOT EXISTS buildings (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    apartment_id    UUID            NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    code            VARCHAR(50),                    -- e.g. "A", "B", "Wing-1"
    total_floors    INTEGER         NOT NULL DEFAULT 1,
    total_units     INTEGER         NOT NULL DEFAULT 0,
    description     VARCHAR(1000),
    building_type   VARCHAR(50)     NOT NULL DEFAULT 'RESIDENTIAL',

    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255)    NOT NULL DEFAULT 'SYSTEM',
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT pk_buildings PRIMARY KEY (id),
    CONSTRAINT fk_buildings_apartment FOREIGN KEY (apartment_id) REFERENCES apartments (id),
    CONSTRAINT uq_buildings_apartment_code UNIQUE (apartment_id, code),
    CONSTRAINT chk_buildings_status CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED','PENDING','DELETED')),
    CONSTRAINT chk_buildings_type CHECK (building_type IN ('RESIDENTIAL','COMMERCIAL','MIXED'))
);

CREATE INDEX IF NOT EXISTS idx_buildings_apartment_id
    ON buildings (apartment_id) WHERE is_deleted = FALSE;


-- ── 3. apartment_settings ─────────────────────────────────────
-- Per-tenant configurable settings (override system_configurations).

CREATE TABLE IF NOT EXISTS apartment_settings (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    apartment_id    UUID            NOT NULL,
    setting_key     VARCHAR(255)    NOT NULL,
    setting_value   TEXT,
    description     VARCHAR(500),
    data_type       VARCHAR(50)     NOT NULL DEFAULT 'STRING',

    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(255)    NOT NULL DEFAULT 'SYSTEM',
    updated_by      VARCHAR(255),
    version         BIGINT          NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT pk_apartment_settings PRIMARY KEY (id),
    CONSTRAINT fk_apt_settings_apartment FOREIGN KEY (apartment_id) REFERENCES apartments (id),
    CONSTRAINT uq_apt_settings_key UNIQUE (apartment_id, setting_key)
);

CREATE INDEX IF NOT EXISTS idx_apt_settings_apartment
    ON apartment_settings (apartment_id, setting_key) WHERE is_deleted = FALSE;

-- ── 4. Add apartment FK to users table ────────────────────────
-- Now that apartments table exists, add the FK constraint.
-- (We left it as a plain UUID column in V2 to keep migration order clean.)
ALTER TABLE users
    ADD CONSTRAINT fk_users_apartment
    FOREIGN KEY (apartment_id) REFERENCES apartments (id)
    NOT VALID;   -- NOT VALID avoids locking; validated lazily by Postgres
