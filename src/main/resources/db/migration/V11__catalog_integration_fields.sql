-- ── Product: novos campos para integração ──
ALTER TABLE product ADD COLUMN external_code VARCHAR(255);
ALTER TABLE product ADD COLUMN catalog_item_type VARCHAR(20) NOT NULL DEFAULT 'DEFAULT';

-- ── Product: migração de boolean active → enum status ──
ALTER TABLE product ADD COLUMN status VARCHAR(20);
UPDATE product SET status = CASE WHEN active = true THEN 'AVAILABLE' ELSE 'UNAVAILABLE' END;
ALTER TABLE product ALTER COLUMN status SET NOT NULL;
ALTER TABLE product ALTER COLUMN status SET DEFAULT 'AVAILABLE';
ALTER TABLE product DROP COLUMN active;

-- ── Product Channel Prices: preços por canal de venda ──
CREATE TABLE product_channel_price (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id  UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    channel     VARCHAR(30) NOT NULL,   -- 'IFOOD', 'RAPPI', 'UBEREATS', etc.
    price       NUMERIC(10,2) NOT NULL,
    UNIQUE(product_id, channel)
);
CREATE INDEX idx_channel_price_product ON product_channel_price(product_id);

-- ── ProductCategory: migração de boolean active → enum status ──
ALTER TABLE product_category ADD COLUMN status VARCHAR(20);
UPDATE product_category SET status = CASE WHEN active = true THEN 'AVAILABLE' ELSE 'UNAVAILABLE' END;
ALTER TABLE product_category ALTER COLUMN status SET NOT NULL;
ALTER TABLE product_category ALTER COLUMN status SET DEFAULT 'AVAILABLE';
ALTER TABLE product_category DROP COLUMN active;

-- ── ModifierGroup: tipo semântico ──
ALTER TABLE modifier_group ADD COLUMN semantic_type VARCHAR(30);

-- ── ModifierOption: referência a produto existente (combos) ──
ALTER TABLE modifier_option ADD COLUMN linked_product_id UUID REFERENCES product(id);

-- ── Índices ──
CREATE UNIQUE INDEX idx_product_external_code_store ON product(store_id, external_code) 
    WHERE external_code IS NOT NULL;
