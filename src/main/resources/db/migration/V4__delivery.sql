-- V4__delivery.sql
-- Entregadores (DeliveryDriver)

CREATE TABLE delivery_driver (
    id          UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    store_id    UUID NOT NULL REFERENCES store(id),
    name        VARCHAR(255) NOT NULL,
    phone       VARCHAR(50),
    status      VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    is_fixed    BOOLEAN NOT NULL DEFAULT false,
    is_daily    BOOLEAN NOT NULL DEFAULT false,
    allowance   NUMERIC(10,2) NOT NULL DEFAULT 0,
    has_fixed_delivery_value BOOLEAN NOT NULL DEFAULT false,
    fixed_delivery_value     NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_deliveries INTEGER NOT NULL DEFAULT 0,
    rating      NUMERIC(3,1) NOT NULL DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_delivery_driver_store ON delivery_driver(store_id);
