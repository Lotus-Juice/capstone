-- ============================================================================
-- File: 005_shipping.sql
-- Module: Shipping & Logistics Fulfillment Domain
-- ============================================================================

-- Table: shipments
CREATE TABLE IF NOT EXISTS shipments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID NOT NULL,
    carrier_name        VARCHAR(100) NOT NULL,
    tracking_number     VARCHAR(100) NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'MANIFESTED', -- MANIFESTED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED_ATTEMPT, RETURNED_TO_ORIGIN
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

-- Table: shipment_checkpoints
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
