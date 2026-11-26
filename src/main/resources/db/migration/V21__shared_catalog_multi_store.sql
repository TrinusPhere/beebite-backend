-- ═══════════════════════════════════════════════════════════════════════════
-- V21__shared_catalog_multi_store.sql
-- Arquitetura Enterprise de Cardápio Compartilhado (Tenant 1:N Catalog, Store N:1 Catalog)
-- + Estado Operacional por Loja (Estoque, Pausa Local, Setor KDS)
-- + Cursor de Pull Sync de Catálogo para Nós Edge
-- ═══════════════════════════════════════════════════════════════════════════

-- ── 1. Garantir que toda store possua um tenant_id válido antes do backfill ──
UPDATE store
SET tenant_id = '00000000-0000-0000-0000-000000000001'
WHERE tenant_id IS NULL;

-- ── 2. Criar tabela catalog (Pertence ao TenantAccount) ─────────────────────
CREATE TABLE catalog (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL REFERENCES tenant_account(id) ON DELETE CASCADE,
    name        VARCHAR(150) NOT NULL,
    is_default  BOOLEAN      NOT NULL DEFAULT false,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_catalog_tenant ON catalog(tenant_id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_catalog_default_per_tenant
    ON catalog(tenant_id)
    WHERE is_default = true AND deleted_at IS NULL;

-- Cria 1 Catalog padrão para cada TenantAccount existente
INSERT INTO catalog (id, tenant_id, name, is_default, created_at, updated_at)
SELECT
    CASE
        WHEN t.id = '00000000-0000-0000-0000-000000000001' THEN '00000000-0000-0000-0000-000000000001'::uuid
        ELSE gen_random_uuid()
    END,
    t.id,
    left('Cardápio Principal - ' || t.trade_name, 150),
    true,
    now(),
    now()
FROM tenant_account t;

-- ── 3. Vincular store -> catalog + cursor last_catalog_pull_at ──────────────
ALTER TABLE store
    ADD COLUMN catalog_id UUID REFERENCES catalog(id),
    ADD COLUMN last_catalog_pull_at TIMESTAMP;

UPDATE store s
SET catalog_id = c.id
FROM catalog c
WHERE s.tenant_id = c.tenant_id AND c.is_default = true;

ALTER TABLE store
    ALTER COLUMN catalog_id SET NOT NULL;

CREATE INDEX idx_store_catalog_id ON store(catalog_id);

-- ── 4. Tabelas de Estado Operacional por Loja (Estoque / Pausa / Setor KDS) ─
CREATE TABLE store_product_setting (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id             UUID        NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    product_id           UUID        NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    stock                INTEGER     NOT NULL DEFAULT 0,
    local_status         VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
        CHECK (local_status IN ('AVAILABLE', 'UNAVAILABLE')),
    production_sector_id UUID        REFERENCES production_sector(id) ON DELETE SET NULL,
    updated_at           TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT uq_store_product_setting UNIQUE (store_id, product_id)
);

CREATE INDEX idx_store_product_setting_store ON store_product_setting(store_id);
CREATE INDEX idx_store_product_setting_product ON store_product_setting(product_id);

-- Backfill do estado operacional atual de cada produto em sua loja original
INSERT INTO store_product_setting (store_id, product_id, stock, local_status, production_sector_id, updated_at)
SELECT p.store_id, p.id, COALESCE(p.stock, 0), 'AVAILABLE', p.production_sector_id, now()
FROM product p
ON CONFLICT (store_id, product_id) DO NOTHING;

CREATE TABLE store_ingredient_stock (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id      UUID          NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    ingredient_id UUID          NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    stock         DECIMAL(12,3) NOT NULL DEFAULT 0,
    min_stock     DECIMAL(12,3) NOT NULL DEFAULT 0,
    updated_at    TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT uq_store_ingredient_stock UNIQUE (store_id, ingredient_id)
);

CREATE INDEX idx_store_ingredient_stock_store ON store_ingredient_stock(store_id);

-- Backfill do saldo de insumos atual em sua loja original
INSERT INTO store_ingredient_stock (store_id, ingredient_id, stock, min_stock, updated_at)
SELECT i.store_id, i.id, COALESCE(i.stock, 0), COALESCE(i.min_stock, 0), now()
FROM ingredient i
ON CONFLICT (store_id, ingredient_id) DO NOTHING;

-- ── 5. Migrar tabelas de definição de Catálogo de store_id para catalog_id ──

-- 5.1 product_category (garantindo updated_at para Delta Sync)
ALTER TABLE product_category
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN catalog_id UUID REFERENCES catalog(id) ON DELETE CASCADE;

UPDATE product_category pc
SET catalog_id = s.catalog_id
FROM store s
WHERE pc.store_id = s.id;

-- Remove eventuais categorias órfãs de lojas já excluídas fisicamente (V5 não possuía FK para store)
DELETE FROM product_category WHERE catalog_id IS NULL;

ALTER TABLE product_category
    ALTER COLUMN catalog_id SET NOT NULL,
    DROP COLUMN store_id;

CREATE INDEX idx_product_category_catalog_updated ON product_category(catalog_id, updated_at);

-- 5.2 modifier_group
ALTER TABLE modifier_group
    ADD COLUMN catalog_id UUID REFERENCES catalog(id) ON DELETE CASCADE;

UPDATE modifier_group mg
SET catalog_id = s.catalog_id
FROM store s
WHERE mg.store_id = s.id;

ALTER TABLE modifier_group
    ALTER COLUMN catalog_id SET NOT NULL,
    DROP COLUMN store_id;

CREATE INDEX idx_modifier_group_catalog_updated ON modifier_group(catalog_id, updated_at);

-- 5.3 additional
ALTER TABLE additional
    ADD COLUMN catalog_id UUID REFERENCES catalog(id) ON DELETE CASCADE;

UPDATE additional a
SET catalog_id = s.catalog_id
FROM store s
WHERE a.store_id = s.id;

ALTER TABLE additional
    ALTER COLUMN catalog_id SET NOT NULL,
    DROP COLUMN store_id;

CREATE INDEX idx_additional_catalog_updated ON additional(catalog_id, updated_at);

-- 5.4 ingredient (mantém definição cadastral no catalog; stock/min_stock migraram para store_ingredient_stock)
ALTER TABLE ingredient
    ADD COLUMN catalog_id UUID REFERENCES catalog(id) ON DELETE CASCADE;

UPDATE ingredient i
SET catalog_id = s.catalog_id
FROM store s
WHERE i.store_id = s.id;

ALTER TABLE ingredient
    ALTER COLUMN catalog_id SET NOT NULL,
    DROP COLUMN store_id,
    DROP COLUMN stock,
    DROP COLUMN min_stock;

CREATE INDEX idx_ingredient_catalog_updated ON ingredient(catalog_id, updated_at);

-- 5.5 product (mantém definição cadastral + default_sector_code; stock/sector por loja ficam em store_product_setting)
DROP INDEX IF EXISTS idx_product_external_code_store;

ALTER TABLE product
    ADD COLUMN catalog_id UUID REFERENCES catalog(id) ON DELETE CASCADE,
    ADD COLUMN default_sector_code VARCHAR(50) DEFAULT 'KITCHEN';

UPDATE product p
SET catalog_id = s.catalog_id,
    default_sector_code = COALESCE(
        (SELECT ps.code FROM production_sector ps WHERE ps.id = p.production_sector_id),
        'KITCHEN'
    )
FROM store s
WHERE p.store_id = s.id;

-- Normaliza external_code em branco e resolve eventuais colisões ao unificar lojas no mesmo catálogo
UPDATE product
SET external_code = NULL
WHERE external_code IS NOT NULL AND btrim(external_code) = '';

WITH ranked_codes AS (
    SELECT id,
           row_number() OVER (
               PARTITION BY catalog_id, external_code
               ORDER BY updated_at DESC, created_at DESC, id
           ) AS rn
    FROM product
    WHERE external_code IS NOT NULL AND deleted_at IS NULL
)
UPDATE product p
SET external_code = NULL
FROM ranked_codes r
WHERE p.id = r.id AND r.rn > 1;

ALTER TABLE product
    ALTER COLUMN catalog_id SET NOT NULL,
    DROP CONSTRAINT IF EXISTS fk_product_production_sector,
    DROP COLUMN store_id,
    DROP COLUMN stock,
    DROP COLUMN production_sector_id;

CREATE UNIQUE INDEX idx_product_external_code_catalog
    ON product(catalog_id, external_code)
    WHERE external_code IS NOT NULL AND deleted_at IS NULL;

CREATE INDEX idx_product_catalog_updated ON product(catalog_id, updated_at);
