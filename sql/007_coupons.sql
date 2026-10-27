-- ============================================================================
-- File: 007_coupons.sql
-- Module: Pricing, Promotions & Coupon Redemption Domain
-- ============================================================================

-- Table: coupons
CREATE TABLE IF NOT EXISTS coupons (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                    VARCHAR(50) NOT NULL,
    discount_type           VARCHAR(20) NOT NULL, -- PERCENTAGE, FLAT_AMOUNT
    discount_value          NUMERIC(10, 2) NOT NULL,
    min_order_amount        NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    max_discount_amount     NUMERIC(10, 2) NULL,
    usage_limit_per_user    INT NOT NULL DEFAULT 1,
    total_usage_limit       INT NULL,
    valid_from              TIMESTAMPTZ NOT NULL,
    valid_until             TIMESTAMPTZ NOT NULL,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit Columns
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID NULL,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              UUID NULL,
    version                 BIGINT NOT NULL DEFAULT 1,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT chk_coupon_dates 
        CHECK (valid_until > valid_from),
    CONSTRAINT chk_discount_value 
        CHECK (discount_value > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_coupons_code 
    ON coupons (UPPER(code)) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_coupons_validity 
    ON coupons (is_active, valid_from, valid_until) 
    WHERE is_deleted = FALSE;

-- Table: coupon_redemptions
CREATE TABLE IF NOT EXISTS coupon_redemptions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    coupon_id               UUID NOT NULL,
    user_id                 UUID NOT NULL,
    order_id                UUID NOT NULL,
    discount_amount         NUMERIC(10, 2) NOT NULL,

    -- Audit Columns
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID NULL,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              UUID NULL,
    version                 BIGINT NOT NULL DEFAULT 1,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_coupon_redemptions_coupon 
        FOREIGN KEY (coupon_id) REFERENCES coupons(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_coupon_redemptions_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_coupon_redemptions_order 
        FOREIGN KEY (order_id) REFERENCES orders(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_redemption_discount 
        CHECK (discount_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_coupon_redemptions_user_coupon 
    ON coupon_redemptions (user_id, coupon_id) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_coupon_redemptions_order_id 
    ON coupon_redemptions (order_id);

-- Post-table creation: Add Foreign Key from orders.coupon_id to coupons.id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_orders_coupon'
    ) THEN
        ALTER TABLE orders 
            ADD CONSTRAINT fk_orders_coupon 
            FOREIGN KEY (coupon_id) REFERENCES coupons(id) 
            ON DELETE SET NULL ON UPDATE CASCADE;
    END IF;
END $$;
