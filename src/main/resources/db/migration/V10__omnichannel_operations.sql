-- ═══════════════════════════════════════════════════════════════════════════
-- V10__omnichannel_operations.sql
-- BiteFlow — evolução do domínio para operações, estoque rastreável,
-- produção, impressão, integrações, conversas e canal próprio.
-- ═══════════════════════════════════════════════════════════════════════════

-- ── Ingredientes ──────────────────────────────────────────────────────────
ALTER TABLE ingredient
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true;

-- ── Produtos / Produção ───────────────────────────────────────────────────
CREATE TABLE production_sector (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id    UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    code        VARCHAR(50),
    name        VARCHAR(120) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT true,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMP
);

CREATE UNIQUE INDEX idx_production_sector_store_name_active
    ON production_sector(store_id, lower(name)) WHERE deleted_at IS NULL;
CREATE INDEX idx_production_sector_store ON production_sector(store_id);
CREATE INDEX idx_production_sector_deleted_at ON production_sector(deleted_at);

-- Setores padrão para lojas existentes
INSERT INTO production_sector (store_id, code, name)
SELECT id, 'KITCHEN', 'Cozinha' FROM store WHERE deleted_at IS NULL
ON CONFLICT DO NOTHING;
INSERT INTO production_sector (store_id, code, name)
SELECT id, 'BAR', 'Bar' FROM store WHERE deleted_at IS NULL
ON CONFLICT DO NOTHING;
INSERT INTO production_sector (store_id, code, name)
SELECT id, 'DESSERT', 'Sobremesas' FROM store WHERE deleted_at IS NULL
ON CONFLICT DO NOTHING;
INSERT INTO production_sector (store_id, code, name)
SELECT id, 'EXPEDITION', 'Expedição' FROM store WHERE deleted_at IS NULL
ON CONFLICT DO NOTHING;

ALTER TABLE product
    ADD COLUMN IF NOT EXISTS product_type VARCHAR(20) NOT NULL DEFAULT 'PRODUCED',
    ADD COLUMN IF NOT EXISTS track_stock BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS production_sector_id UUID,
    ADD COLUMN IF NOT EXISTS sale_unit VARCHAR(10) NOT NULL DEFAULT 'UN',
    ADD COLUMN IF NOT EXISTS available_digital_menu BOOLEAN NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS available_pdv BOOLEAN NOT NULL DEFAULT true;

ALTER TABLE product
    ADD CONSTRAINT fk_product_production_sector
    FOREIGN KEY (production_sector_id) REFERENCES production_sector(id) ON DELETE SET NULL;

ALTER TABLE product
    ADD CONSTRAINT chk_product_type
    CHECK (product_type IN ('PRODUCED', 'RESALE'));

-- ── Pedidos / contexto omnichannel ────────────────────────────────────────
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS operation VARCHAR(30),
    ADD COLUMN IF NOT EXISTS channel VARCHAR(40),
    ADD COLUMN IF NOT EXISTS service_type VARCHAR(40),
    ADD COLUMN IF NOT EXISTS source VARCHAR(60),
    ADD COLUMN IF NOT EXISTS customer_phone VARCHAR(30),
    ADD COLUMN IF NOT EXISTS customer_address_complement VARCHAR(150),
    ADD COLUMN IF NOT EXISTS customer_address_reference VARCHAR(255),
    ADD COLUMN IF NOT EXISTS cash_change_for DECIMAL(12,2),
    ADD COLUMN IF NOT EXISTS notes TEXT;

-- Compatibilidade: classifica registros existentes
UPDATE orders
SET operation = CASE WHEN type = 'DELIVERY' THEN 'DELIVERY' ELSE 'SALAO' END
WHERE operation IS NULL;

UPDATE orders
SET service_type = CASE WHEN type = 'DELIVERY' THEN 'ENTREGA' ELSE 'CONSUMO_LOCAL' END
WHERE service_type IS NULL;

UPDATE orders
SET channel = CASE
    WHEN platform = 'IFOOD' THEN 'IFOOD'
    WHEN platform = 'WHATSAPP' THEN 'WHATSAPP'
    ELSE 'PDV'
END
WHERE channel IS NULL;

UPDATE orders
SET source = CASE
    WHEN platform = 'IFOOD' THEN 'INTEGRACAO_IFOOD'
    WHEN platform = 'WHATSAPP' THEN 'ATENDIMENTO_WHATSAPP'
    ELSE 'PDV_CAIXA'
END
WHERE source IS NULL;

ALTER TABLE orders ALTER COLUMN operation SET NOT NULL;
ALTER TABLE orders ALTER COLUMN channel SET NOT NULL;
ALTER TABLE orders ALTER COLUMN service_type SET NOT NULL;
ALTER TABLE orders ALTER COLUMN source SET NOT NULL;

CREATE INDEX idx_orders_operation ON orders(store_id, operation);
CREATE INDEX idx_orders_channel ON orders(store_id, channel);
CREATE INDEX idx_orders_service_type ON orders(store_id, service_type);

-- ── Movimentações de Estoque ──────────────────────────────────────────────
CREATE TABLE stock_movement (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id         UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    ingredient_id    UUID NOT NULL REFERENCES ingredient(id),
    type             VARCHAR(30) NOT NULL,
    quantity         DECIMAL(12,3) NOT NULL,
    previous_stock   DECIMAL(12,3) NOT NULL,
    resulting_stock  DECIMAL(12,3) NOT NULL,
    reason           VARCHAR(255) NOT NULL,
    document_ref     VARCHAR(255),
    source_type      VARCHAR(50),
    source_id        UUID,
    created_by       UUID,
    created_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_stock_movement_store_created ON stock_movement(store_id, created_at DESC);
CREATE INDEX idx_stock_movement_ingredient ON stock_movement(ingredient_id, created_at DESC);

-- ── Impressoras e regras de roteamento ────────────────────────────────────
CREATE TABLE printer (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id         UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    name             VARCHAR(120) NOT NULL,
    connection_type  VARCHAR(30) NOT NULL,
    address          VARCHAR(255),
    active           BOOLEAN NOT NULL DEFAULT true,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at       TIMESTAMP
);
CREATE INDEX idx_printer_store ON printer(store_id);
CREATE INDEX idx_printer_deleted_at ON printer(deleted_at);

CREATE TABLE print_rule (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id       UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    name           VARCHAR(120) NOT NULL,
    operation      VARCHAR(30) NOT NULL,
    document_type  VARCHAR(30) NOT NULL,
    sector_id      UUID REFERENCES production_sector(id) ON DELETE SET NULL,
    printer_id     UUID NOT NULL REFERENCES printer(id),
    active         BOOLEAN NOT NULL DEFAULT true,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMP
);
CREATE INDEX idx_print_rule_store_operation ON print_rule(store_id, operation);
CREATE INDEX idx_print_rule_deleted_at ON print_rule(deleted_at);

-- ── Configurações de Integração ───────────────────────────────────────────
CREATE TABLE integration_config (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id             UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    provider             VARCHAR(30) NOT NULL,
    status               VARCHAR(30) NOT NULL DEFAULT 'disconnected',
    external_store_id    VARCHAR(255),
    client_id            VARCHAR(500),
    client_secret        TEXT,
    access_token         TEXT,
    business_account_id  VARCHAR(255),
    phone_number         VARCHAR(50),
    api_provider         VARCHAR(80),
    store_status         VARCHAR(30),
    created_at           TIMESTAMP NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_integration_store_provider UNIQUE (store_id, provider)
);

-- ── Conversas e mensagens ─────────────────────────────────────────────────
CREATE TABLE conversation (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    order_id        UUID REFERENCES orders(id) ON DELETE SET NULL,
    channel         VARCHAR(40) NOT NULL,
    operation       VARCHAR(30) NOT NULL DEFAULT 'DELIVERY',
    customer_name   VARCHAR(255),
    customer_phone  VARCHAR(50),
    status          VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    external_id     VARCHAR(255),
    unread_count    INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMP
);

CREATE UNIQUE INDEX idx_conversation_order_channel
    ON conversation(store_id, order_id, channel)
    WHERE order_id IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_conversation_store_updated ON conversation(store_id, updated_at DESC);
CREATE INDEX idx_conversation_deleted_at ON conversation(deleted_at);

CREATE TABLE conversation_message (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id  UUID NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
    direction        VARCHAR(20) NOT NULL,
    content          TEXT NOT NULL,
    delivery_status  VARCHAR(30),
    external_id      VARCHAR(255),
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    read_at          TIMESTAMP
);
CREATE INDEX idx_conversation_message_conversation ON conversation_message(conversation_id, created_at);

-- ── Identificador público da loja ─────────────────────────────────────────
ALTER TABLE store ADD COLUMN IF NOT EXISTS public_slug VARCHAR(255);

-- Gera slugs legíveis e evita falha da migration quando duas lojas possuem o mesmo nome.
WITH slugged AS (
    SELECT id,
           lower(regexp_replace(regexp_replace(name, '[^a-zA-Z0-9]+', '-', 'g'), '(^-|-$)', '', 'g')) AS base_slug,
           row_number() OVER (
               PARTITION BY lower(regexp_replace(regexp_replace(name, '[^a-zA-Z0-9]+', '-', 'g'), '(^-|-$)', '', 'g'))
               ORDER BY created_at, id
           ) AS rn
    FROM store
    WHERE public_slug IS NULL
)
UPDATE store s
SET public_slug = CASE
    WHEN slugged.rn = 1 THEN slugged.base_slug
    ELSE slugged.base_slug || '-' || substring(s.id::text, 1, 8)
END
FROM slugged
WHERE s.id = slugged.id;

CREATE UNIQUE INDEX IF NOT EXISTS idx_store_public_slug_unique
    ON store(public_slug) WHERE deleted_at IS NULL;
