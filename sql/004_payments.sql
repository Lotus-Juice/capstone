-- ============================================================================
-- File: 004_payments.sql
-- Module: Payment Processing & Settlements Domain
-- ============================================================================

-- Table: payment_transactions
CREATE TABLE IF NOT EXISTS payment_transactions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                UUID NOT NULL,
    gateway_transaction_id  VARCHAR(255) NULL,
    payment_method          VARCHAR(30) NOT NULL, -- CREDIT_CARD, DEBIT_CARD, UPI, WALLET
    payment_gateway         VARCHAR(50) NOT NULL,
    amount                  NUMERIC(10, 2) NOT NULL,
    currency                VARCHAR(3) NOT NULL DEFAULT 'INR',
    status                  VARCHAR(30) NOT NULL DEFAULT 'INITIATED', -- INITIATED, AUTHORIZED, CAPTURED, FAILED
    card_last_four          VARCHAR(4) NULL,
    upi_vpa                 VARCHAR(100) NULL,
    gateway_response_code   VARCHAR(100) NULL,
    gateway_response_raw    JSONB NULL,
    paid_at                 TIMESTAMPTZ NULL,

    -- Audit Columns
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID NULL,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              UUID NULL,
    version                 BIGINT NOT NULL DEFAULT 1,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_payment_transactions_order 
        FOREIGN KEY (order_id) REFERENCES orders(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_payment_amount 
        CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_payment_transactions_order_id 
    ON payment_transactions (order_id) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_payment_transactions_gateway_id 
    ON payment_transactions (gateway_transaction_id) 
    WHERE gateway_transaction_id IS NOT NULL;

-- Table: payment_refunds
CREATE TABLE IF NOT EXISTS payment_refunds (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_transaction_id  UUID NOT NULL,
    gateway_refund_id       VARCHAR(255) NULL,
    amount                  NUMERIC(10, 2) NOT NULL,
    reason                  VARCHAR(255) NOT NULL,
    status                  VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSED, FAILED
    processed_at            TIMESTAMPTZ NULL,

    -- Audit Columns
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID NULL,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              UUID NULL,
    version                 BIGINT NOT NULL DEFAULT 1,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_payment_refunds_payment 
        FOREIGN KEY (payment_transaction_id) REFERENCES payment_transactions(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_refund_amount 
        CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_payment_refunds_payment_id 
    ON payment_refunds (payment_transaction_id) 
    WHERE is_deleted = FALSE;
