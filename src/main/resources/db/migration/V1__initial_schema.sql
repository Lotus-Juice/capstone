-- ============================================================================
-- Migration: V1__initial_schema.sql
-- Description: Complete Relational Schema (PostgreSQL 3NF DDL) for Bookworm Platform
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ----------------------------------------------------------------------------
-- 1. Identity & Access Management (Users & Addresses)
-- ----------------------------------------------------------------------------

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

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email 
    ON users (LOWER(email)) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_users_role 
    ON users (role) 
    WHERE is_deleted = FALSE;

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

CREATE INDEX IF NOT EXISTS idx_user_addresses_user_id 
    ON user_addresses (user_id) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_user_addresses_pincode 
    ON user_addresses (pincode);

-- ----------------------------------------------------------------------------
-- 2. Catalog & Merchandising (Publishers, Authors, Categories, Books, Variants)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS publishers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(200) NOT NULL,
    slug                VARCHAR(255) NOT NULL,
    description         TEXT NULL,
    website             VARCHAR(255) NULL,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_publishers_slug 
    ON publishers (slug) 
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS authors (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(200) NOT NULL,
    slug                VARCHAR(255) NOT NULL,
    biography           TEXT NULL,
    profile_image_url   VARCHAR(500) NULL,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_authors_slug 
    ON authors (slug) 
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS categories (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id           UUID NULL,
    name                VARCHAR(100) NOT NULL,
    slug                VARCHAR(150) NOT NULL,
    description         TEXT NULL,
    display_order       INT NOT NULL DEFAULT 0,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_categories_parent 
        FOREIGN KEY (parent_id) REFERENCES categories(id) 
        ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_categories_slug 
    ON categories (slug) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_categories_parent_order 
    ON categories (parent_id, display_order) 
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS books (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    publisher_id        UUID NULL,
    title               VARCHAR(300) NOT NULL,
    subtitle            VARCHAR(300) NULL,
    slug                VARCHAR(350) NOT NULL,
    synopsis            TEXT NOT NULL,
    language            VARCHAR(50) NOT NULL DEFAULT 'English',
    cover_image_url     VARCHAR(500) NOT NULL,
    published_date      DATE NULL,
    copies_sold         INT NOT NULL DEFAULT 0,
    average_rating      NUMERIC(3, 2) NOT NULL DEFAULT 0.00,
    ratings_count       INT NOT NULL DEFAULT 0,
    is_featured         BOOLEAN NOT NULL DEFAULT FALSE,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_books_publisher 
        FOREIGN KEY (publisher_id) REFERENCES publishers(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_books_slug 
    ON books (slug) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_books_publisher_id 
    ON books (publisher_id) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_books_rating_sales 
    ON books (average_rating DESC, copies_sold DESC) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_books_published_date 
    ON books (published_date DESC) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_books_is_featured 
    ON books (is_featured) 
    WHERE is_featured = TRUE AND is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS book_authors (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id             UUID NOT NULL,
    author_id           UUID NOT NULL,
    author_order        INT NOT NULL DEFAULT 1,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_book_authors_book 
        FOREIGN KEY (book_id) REFERENCES books(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_book_authors_author 
        FOREIGN KEY (author_id) REFERENCES authors(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_book_authors_pair 
    ON book_authors (book_id, author_id) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_book_authors_author_id 
    ON book_authors (author_id);

CREATE TABLE IF NOT EXISTS book_categories (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id             UUID NOT NULL,
    category_id         UUID NOT NULL,
    is_primary          BOOLEAN NOT NULL DEFAULT FALSE,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_book_categories_book 
        FOREIGN KEY (book_id) REFERENCES books(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_book_categories_category 
        FOREIGN KEY (category_id) REFERENCES categories(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_book_categories_pair 
    ON book_categories (book_id, category_id) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_book_categories_category_id 
    ON book_categories (category_id);

CREATE TABLE IF NOT EXISTS book_variants (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id             UUID NOT NULL,
    format_type         VARCHAR(30) NOT NULL,
    isbn                VARCHAR(20) NOT NULL,
    sku                 VARCHAR(50) NOT NULL,
    list_price          NUMERIC(10, 2) NOT NULL,
    selling_price       NUMERIC(10, 2) NOT NULL,
    currency            VARCHAR(3) NOT NULL DEFAULT 'INR',
    stock_quantity      INT NOT NULL DEFAULT 0,
    weight_grams        INT NULL,
    page_count          INT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_book_variants_book 
        FOREIGN KEY (book_id) REFERENCES books(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_book_variants_isbn 
    ON book_variants (isbn) 
    WHERE is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uq_book_variants_sku 
    ON book_variants (sku) 
    WHERE is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uq_book_variants_book_format 
    ON book_variants (book_id, format_type) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_book_variants_book_id 
    ON book_variants (book_id) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_book_variants_selling_price 
    ON book_variants (selling_price) 
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS wishlists (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    book_id             UUID NOT NULL,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_wishlists_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_wishlists_book 
        FOREIGN KEY (book_id) REFERENCES books(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_wishlists_user_book 
    ON wishlists (user_id, book_id) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_wishlists_user_id 
    ON wishlists (user_id);

CREATE TABLE IF NOT EXISTS author_follows (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    author_id           UUID NOT NULL,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_author_follows_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_author_follows_author 
        FOREIGN KEY (author_id) REFERENCES authors(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_author_follows_pair 
    ON author_follows (user_id, author_id) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_author_follows_user_id 
    ON author_follows (user_id);

-- ----------------------------------------------------------------------------
-- 3. Carts, Pricing, Coupons, Orders & Fulfillment
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS carts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NULL,
    guest_session_token VARCHAR(255) NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
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

CREATE TABLE IF NOT EXISTS orders (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number            VARCHAR(50) NOT NULL,
    user_id                 UUID NOT NULL,
    shipping_address_id     UUID NOT NULL,
    coupon_id               UUID NULL,
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
    CONSTRAINT fk_orders_coupon 
        FOREIGN KEY (coupon_id) REFERENCES coupons(id) 
        ON DELETE SET NULL ON UPDATE CASCADE,
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

-- ----------------------------------------------------------------------------
-- 4. Payments, Settlements, Shipping, Reviews
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS payment_transactions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                UUID NOT NULL,
    gateway_transaction_id  VARCHAR(255) NULL,
    payment_method          VARCHAR(30) NOT NULL,
    payment_gateway         VARCHAR(50) NOT NULL,
    amount                  NUMERIC(10, 2) NOT NULL,
    currency                VARCHAR(3) NOT NULL DEFAULT 'INR',
    status                  VARCHAR(30) NOT NULL DEFAULT 'INITIATED',
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

CREATE TABLE IF NOT EXISTS payment_refunds (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_transaction_id  UUID NOT NULL,
    gateway_refund_id       VARCHAR(255) NULL,
    amount                  NUMERIC(10, 2) NOT NULL,
    reason                  VARCHAR(255) NOT NULL,
    status                  VARCHAR(30) NOT NULL DEFAULT 'PENDING',
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

CREATE TABLE IF NOT EXISTS shipments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID NOT NULL,
    carrier_name        VARCHAR(100) NOT NULL,
    tracking_number     VARCHAR(100) NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'MANIFESTED',
    dispatched_at       TIMESTAMPTZ NULL,
    delivered_at        TIMESTAMPTZ NULL,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_shipments_order 
        FOREIGN KEY (order_id) REFERENCES orders(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_shipments_tracking 
    ON shipments (carrier_name, tracking_number) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_shipments_order_id 
    ON shipments (order_id) 
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS shipment_checkpoints (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id         UUID NOT NULL,
    checkpoint_status   VARCHAR(50) NOT NULL,
    location_city       VARCHAR(100) NOT NULL,
    activity_description VARCHAR(255) NOT NULL,
    scanned_at          TIMESTAMPTZ NOT NULL,

    -- Audit Columns
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID NULL,
    version             BIGINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_shipment_checkpoints_shipment 
        FOREIGN KEY (shipment_id) REFERENCES shipments(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_shipment_checkpoints_shipment_scanned 
    ON shipment_checkpoints (shipment_id, scanned_at ASC) 
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS reviews (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id                 UUID NOT NULL,
    user_id                 UUID NOT NULL,
    rating                  SMALLINT NOT NULL,
    review_text             VARCHAR(500) NULL,
    is_verified_purchase    BOOLEAN NOT NULL DEFAULT FALSE,
    moderation_status       VARCHAR(30) NOT NULL DEFAULT 'APPROVED',

    -- Audit Columns
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              UUID NULL,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              UUID NULL,
    version                 BIGINT NOT NULL DEFAULT 1,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_reviews_book 
        FOREIGN KEY (book_id) REFERENCES books(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reviews_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_review_rating 
        CHECK (rating >= 1 AND rating <= 5)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_reviews_user_book 
    ON reviews (user_id, book_id) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_reviews_book_moderation 
    ON reviews (book_id, moderation_status, created_at DESC) 
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_reviews_user_id 
    ON reviews (user_id);
