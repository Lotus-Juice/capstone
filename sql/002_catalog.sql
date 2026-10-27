-- ============================================================================
-- File: 002_catalog.sql
-- Module: Catalog & Merchandising Domain
-- ============================================================================

-- Table: publishers
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

-- Table: authors
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

-- Table: categories
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

-- Table: books
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

-- Table: book_authors (Junction Table)
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

-- Table: book_categories (Junction Table)
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

-- Table: book_variants
CREATE TABLE IF NOT EXISTS book_variants (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id             UUID NOT NULL,
    format_type         VARCHAR(30) NOT NULL, -- PAPERBACK, HARDCOVER, EBOOK, AUDIO
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

-- Table: wishlists
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

-- Table: author_follows ("My Writers")
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
