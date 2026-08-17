-- Add version column to orders table for Optimistic Locking
ALTER TABLE orders ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Add version column to product table for Optimistic Locking
ALTER TABLE product ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
