-- ============================================================================
-- File: 001_users.sql
-- Module: Identity & Access Management Domain
-- ============================================================================

-- Ensure pgcrypto extension is available for UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Table: users
CREATE TABLE IF NOT EXISTS users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(255) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    phone_number        VARCHAR(20) NULL,
    role                VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at       TIMESTAMPTZ NULL,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE
);

-- Unique & Operational Indexes for users
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email 
    ON users (LOWER(email)) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_users_role 
    ON users (role) 
    WHERE is_deleted = FALSE;

-- Table: user_addresses
CREATE TABLE IF NOT EXISTS user_addresses (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    recipient_name      VARCHAR(200) NOT NULL,
    recipient_phone     VARCHAR(20) NOT NULL,
    address_line1       VARCHAR(255) NOT NULL,
    address_line2       VARCHAR(255) NULL,
    city                VARCHAR(100) NOT NULL,
    state               VARCHAR(100) NOT NULL,
    pincode             VARCHAR(20) NOT NULL,
    country             VARCHAR(100) NOT NULL DEFAULT 'India',
    is_default          BOOLEAN NOT NULL DEFAULT FALSE,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_user_addresses_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Operational Indexes for user_addresses
CREATE INDEX IF NOT EXISTS idx_user_addresses_user_id 
    ON user_addresses (user_id) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_user_addresses_pincode 
    ON user_addresses (pincode);
