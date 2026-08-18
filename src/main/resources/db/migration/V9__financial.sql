-- V9__financial.sql
-- Fase 5: Financeiro, CMV & Dashboard

-- ═══════════════════════════════════════════════════════════════
-- Tabela: financial_record
-- Registro financeiro gerado automaticamente ao finalizar pedido
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE financial_record (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id   UUID         NOT NULL REFERENCES store(id),
    order_id   UUID         NOT NULL REFERENCES orders(id),
    record_date DATE        NOT NULL,
    record_time TIME        NOT NULL,
    total_value DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'CONCLUIDO',
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_financial_record_store_date ON financial_record(store_id, record_date);
CREATE INDEX idx_financial_record_order ON financial_record(order_id);

-- ═══════════════════════════════════════════════════════════════
-- Tabela: financial_record_product
-- Snapshot dos produtos vendidos no registro financeiro
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE financial_record_product (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    financial_record_id UUID          NOT NULL REFERENCES financial_record(id) ON DELETE CASCADE,
    product_id          UUID,
    product_name        VARCHAR(200)  NOT NULL,
    quantity            INTEGER       NOT NULL,
    price               DECIMAL(12,2) NOT NULL
);

CREATE INDEX idx_frp_record ON financial_record_product(financial_record_id);
