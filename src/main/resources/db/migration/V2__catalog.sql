-- ═══════════════════════════════════════════════════
-- V2__catalog.sql — BiteFlow Fase 2: Catálogo
-- ═══════════════════════════════════════════════════

-- ── Ingredientes ──────────────────────────────────
CREATE TABLE ingredient (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID         NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    stock           DECIMAL(12,3) NOT NULL DEFAULT 0,
    unit            VARCHAR(10)  NOT NULL DEFAULT 'UN',
    cost_price      DECIMAL(10,2) NOT NULL DEFAULT 0,
    min_stock       DECIMAL(12,3) NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

-- ── Produtos ──────────────────────────────────────
CREATE TABLE product (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID         NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    category        VARCHAR(100),
    description     TEXT,
    price           DECIMAL(10,2) NOT NULL DEFAULT 0,
    image_url       VARCHAR(500),
    stock           INTEGER      NOT NULL DEFAULT 0,
    active          BOOLEAN      NOT NULL DEFAULT true,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

-- ── Ficha Técnica (Produto ↔ Ingrediente) ─────────
CREATE TABLE technical_sheet_item (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID         NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    ingredient_id   UUID         NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    quantity        DECIMAL(10,3) NOT NULL DEFAULT 1,
    cost_snapshot   DECIMAL(10,2) NOT NULL DEFAULT 0,
    UNIQUE(product_id, ingredient_id)
);

-- ── Itens Removíveis do Produto ───────────────────
CREATE TABLE product_removable_item (
    product_id      UUID         NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    ingredient_id   UUID         NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, ingredient_id)
);

-- ── Grupos de Modificadores ───────────────────────
CREATE TABLE modifier_group (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID         NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(20)  NOT NULL CHECK (type IN ('RADIO','CHECKBOX')),
    required        BOOLEAN      NOT NULL DEFAULT false,
    min_selections  INTEGER      NOT NULL DEFAULT 0,
    max_selections  INTEGER      NOT NULL DEFAULT 1,
    icon            VARCHAR(10),
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

-- ── Opções de Modificador ─────────────────────────
CREATE TABLE modifier_option (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    modifier_group_id UUID       NOT NULL REFERENCES modifier_group(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    price           DECIMAL(10,2) NOT NULL DEFAULT 0,
    sort_order      INTEGER      NOT NULL DEFAULT 0
);

-- ── Produto ↔ Grupo de Modificadores ──────────────
CREATE TABLE product_modifier_group (
    product_id      UUID         NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    modifier_group_id UUID       NOT NULL REFERENCES modifier_group(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, modifier_group_id)
);

-- ── Adicionais Globais ────────────────────────────
CREATE TABLE additional (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID         NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    price           DECIMAL(10,2) NOT NULL DEFAULT 0,
    category        VARCHAR(100),
    active          BOOLEAN      NOT NULL DEFAULT true,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);
