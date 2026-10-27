-- ============================================================================
-- File: 006_reviews.sql
-- Module: Customer Reviews & Ratings Domain
-- ============================================================================

-- Table: reviews
CREATE TABLE IF NOT EXISTS reviews (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id                 UUID NOT NULL,
    user_id                 UUID NOT NULL,
    rating                  SMALLINT NOT NULL,
    review_text             VARCHAR(500) NULL,
    is_verified_purchase    BOOLEAN NOT NULL DEFAULT FALSE,
    moderation_status       VARCHAR(30) NOT NULL DEFAULT 'APPROVED', -- PENDING, APPROVED, REJECTED

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
