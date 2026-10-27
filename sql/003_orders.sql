-- ============================================================================
-- File: 003_orders.sql
-- Module: Shopping Cart & Order Lifecycle Domain
-- ============================================================================

-- Table: carts
CREATE TABLE IF NOT EXISTS carts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NULL,
    guest_session_token VARCHAR(255) NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, MERGED, CHECKED_OUT, ABANDONED
    expires_at          TIMESTAMPTZ NOT NULL,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_carts_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_carts_user_id_active 
    ON carts (user_id) 
    WHERE status = 'ACTIVE' AND is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_carts_guest_token_active 
    ON carts (guest_session_token) 
    WHERE status = 'ACTIVE' AND is_deleted = FALSE;

-- Table: cart_items
CREATE TABLE IF NOT EXISTS cart_items (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id                 UUID NOT NULL,
    variant_id              UUID NOT NULL,
    quantity                INT NOT NULL DEFAULT 1,
    unit_price_at_addition  NUMERIC(10, 2) NOT NULL,

    -- Audit Columns
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID NULL,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              UUID NULL,
    version                 BIGINT NOT NULL DEFAULT 1,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_cart_items_cart 
        FOREIGN KEY (cart_id) REFERENCES carts(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cart_items_variant 
        FOREIGN KEY (variant_id) REFERENCES book_variants(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_cart_item_quantity 
        CHECK (quantity >= 1 AND quantity <= 20)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_cart_items_variant 
    ON cart_items (cart_id, variant_id) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id 
    ON cart_items (cart_id) 
    WHERE is_deleted = FALSE;

-- Table: orders
CREATE TABLE IF NOT EXISTS orders (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number            VARCHAR(50) NOT NULL,
    user_id                 UUID NOT NULL,
    shipping_address_id     UUID NOT NULL,
    coupon_id               UUID NULL, -- Foreign Key to coupons(id) added in 007_coupons.sql
    items_subtotal          NUMERIC(10, 2) NOT NULL,
    tax_amount              NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    shipping_fee            NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    discount_amount         NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    grand_total             NUMERIC(10, 2) NOT NULL,
    currency                VARCHAR(3) NOT NULL DEFAULT 'INR',
    status                  VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    estimated_delivery_date DATE NULL,
    placed_at               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Audit Columns
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID NULL,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              UUID NULL,
    version                 BIGINT NOT NULL DEFAULT 1,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_orders_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_orders_shipping_address 
        FOREIGN KEY (shipping_address_id) REFERENCES user_addresses(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_orders_grand_total 
        CHECK (grand_total >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_orders_number 
    ON orders (order_number) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_orders_user_id_placed 
    ON orders (user_id, placed_at DESC) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_orders_status 
    ON orders (status) 
    WHERE is_deleted = FALSE;

-- Table: order_line_items
CREATE TABLE IF NOT EXISTS order_line_items (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                UUID NOT NULL,
    variant_id              UUID NOT NULL,
    book_title_snapshot     VARCHAR(300) NOT NULL,
    author_name_snapshot    VARCHAR(200) NOT NULL,
    format_type_snapshot    VARCHAR(30) NOT NULL,
    unit_price              NUMERIC(10, 2) NOT NULL,
    quantity                INT NOT NULL,
    line_total              NUMERIC(10, 2) NOT NULL,

    -- Audit Columns
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID NULL,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              UUID NULL,
    version                 BIGINT NOT NULL DEFAULT 1,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_order_line_items_order 
        FOREIGN KEY (order_id) REFERENCES orders(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_order_line_items_variant 
        FOREIGN KEY (variant_id) REFERENCES book_variants(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_order_line_item_qty 
        CHECK (quantity >= 1)
);

CREATE INDEX IF NOT EXISTS idx_order_line_items_order_id 
    ON order_line_items (order_id) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_order_line_items_variant_id 
    ON order_line_items (variant_id);
