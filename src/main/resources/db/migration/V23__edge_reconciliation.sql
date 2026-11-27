-- ═══════════════════════════════════════════════════════════════════════════
-- V23 — Reconciliação Periódica Anti-Entropy Edge <-> Nuvem (Camadas 3 e 4)
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS edge_reconciliation_run (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id              UUID         NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    trigger               VARCHAR(20)  NOT NULL CHECK (trigger IN ('SCHEDULED', 'FULL_RESYNC', 'MANUAL')),
    window_from           DATE         NOT NULL,
    window_to             DATE         NOT NULL,
    status                VARCHAR(20)  NOT NULL CHECK (status IN ('RUNNING', 'CONSISTENT', 'REPAIRED', 'ERROR')),
    buckets_compared      INTEGER      NOT NULL DEFAULT 0,
    buckets_mismatched    INTEGER      NOT NULL DEFAULT 0,
    requeued_count        INTEGER      NOT NULL DEFAULT 0,
    extra_in_cloud_count  INTEGER      NOT NULL DEFAULT 0,
    error_message         TEXT,
    started_at            TIMESTAMP    NOT NULL DEFAULT now(),
    finished_at           TIMESTAMP,
    CHECK (window_from <= window_to)
);

CREATE INDEX IF NOT EXISTS idx_edge_recon_run_store_started
    ON edge_reconciliation_run (store_id, started_at DESC);

-- Índices de suporte às consultas de digest por loja e data
CREATE INDEX IF NOT EXISTS idx_orders_store_created_date     ON orders (store_id, created_at);
CREATE INDEX IF NOT EXISTS idx_payment_store_created         ON payment (store_id, created_at);
CREATE INDEX IF NOT EXISTS idx_cash_movement_store_created   ON cash_movement (store_id, created_at);
CREATE INDEX IF NOT EXISTS idx_cash_session_store_opened     ON cash_session (store_id, opened_at);
CREATE INDEX IF NOT EXISTS idx_financial_record_store_date   ON financial_record (store_id, record_date);

