-- ═══════════════════════════════════════════════════
-- V3__orders_and_customers.sql — BiteFlow Fase 3: Pedidos + Clientes
-- ═══════════════════════════════════════════════════

-- ── Clientes ──────────────────────────────────────
CREATE TABLE customer (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id             UUID         NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    name                 VARCHAR(255) NOT NULL,
    phone                VARCHAR(30),
    email                VARCHAR(255),
    zip_code             VARCHAR(20),
    address_street       VARCHAR(255),
    address_number       VARCHAR(20),
    address_complement   VARCHAR(100),
    address_neighborhood VARCHAR(100),
    total_orders         INTEGER      NOT NULL DEFAULT 0,
    created_at           TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT now()
);

-- ── Pedidos ───────────────────────────────────────
CREATE TABLE orders (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id          UUID         NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    display_id        VARCHAR(20)  NOT NULL,
    customer_id       UUID         REFERENCES customer(id) ON DELETE SET NULL,
    customer_name     VARCHAR(255),
    customer_address  TEXT,
    type              VARCHAR(20)  NOT NULL CHECK (type IN ('BALCAO','DELIVERY')),
    status            VARCHAR(20)  NOT NULL DEFAULT 'ANALISE' CHECK (status IN ('ANALISE','PREPARO','EM_ROTA','FINALIZADO','CANCELADO')),
    payment_method    VARCHAR(20)  NOT NULL CHECK (payment_method IN ('PIX','CREDITO','DEBITO','DINHEIRO')),
    driver_id         UUID,
    driver_name       VARCHAR(255),
    platform          VARCHAR(20)  NOT NULL DEFAULT 'OWN' CHECK (platform IN ('OWN','IFOOD','WHATSAPP')),
    external_order_id VARCHAR(255),
    subtotal          DECIMAL(10,2) NOT NULL DEFAULT 0,
    delivery_cost     DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_value       DECIMAL(10,2) NOT NULL DEFAULT 0,
    nfe_emitted       BOOLEAN      NOT NULL DEFAULT false,
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT now(),
    finished_at       TIMESTAMP
);

-- Sequence para display_id por loja
CREATE SEQUENCE order_display_id_seq START WITH 1000;

-- ── Itens do Pedido ──────────────────────────────
CREATE TABLE order_item (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id      UUID         NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id    UUID         REFERENCES product(id) ON DELETE SET NULL,
    product_name  VARCHAR(255) NOT NULL,
    quantity      INTEGER      NOT NULL DEFAULT 1,
    unit_price    DECIMAL(10,2) NOT NULL,
    extra_price   DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_price   DECIMAL(10,2) NOT NULL,
    observation   TEXT
);

-- ── Modificadores do Item do Pedido ──────────────
CREATE TABLE order_item_modifier (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id     UUID         NOT NULL REFERENCES order_item(id) ON DELETE CASCADE,
    modifier_group_id UUID,
    group_name        VARCHAR(255),
    modifier_option_id UUID,
    option_name       VARCHAR(255),
    price             DECIMAL(10,2) NOT NULL DEFAULT 0
);

-- ── Remoções do Item do Pedido ───────────────────
CREATE TABLE order_item_removal (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id   UUID         NOT NULL REFERENCES order_item(id) ON DELETE CASCADE,
    ingredient_id   UUID,
    ingredient_name VARCHAR(255) NOT NULL
);
