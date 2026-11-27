-- ═══════════════════════════════════════════════════════════════════════════
-- V22 — Confiabilidade da Sincronização Edge -> Nuvem (Camadas 1 e 2)
-- ═══════════════════════════════════════════════════════════════════════════

-- ── LADO EDGE ──────────────────────────────────────────────────────────────

-- 1. Número de sequência monotônico por loja e motivo de rejeição no outbox
ALTER TABLE edge_sync_outbox
    ADD COLUMN IF NOT EXISTS sequence        BIGINT,
    ADD COLUMN IF NOT EXISTS rejected_reason TEXT;

WITH numbered AS (
    SELECT id,
           row_number() OVER (PARTITION BY store_id ORDER BY created_at ASC, id ASC) AS rn
    FROM edge_sync_outbox
)
UPDATE edge_sync_outbox o
SET sequence = n.rn
FROM numbered n
WHERE o.id = n.id
  AND o.sequence IS NULL;

ALTER TABLE edge_sync_outbox
    ALTER COLUMN sequence SET NOT NULL;

ALTER TABLE edge_sync_outbox
    ADD CONSTRAINT uq_edge_outbox_store_sequence UNIQUE (store_id, sequence);

DROP INDEX IF EXISTS idx_edge_outbox_pending;
CREATE INDEX idx_edge_outbox_pending
    ON edge_sync_outbox (store_id, sequence ASC)
    WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_edge_outbox_synced_purge
    ON edge_sync_outbox (synced_at)
    WHERE status = 'SYNCED';

-- 2. Contador transacional de sequência por loja (sem buracos em rollback)
CREATE TABLE IF NOT EXISTS edge_sync_sequence (
    store_id    UUID    PRIMARY KEY REFERENCES store(id) ON DELETE CASCADE,
    last_value  BIGINT  NOT NULL DEFAULT 0 CHECK (last_value >= 0)
);

INSERT INTO edge_sync_sequence (store_id, last_value)
SELECT store_id, MAX(sequence)
FROM edge_sync_outbox
GROUP BY store_id
ON CONFLICT (store_id) DO UPDATE
SET last_value = GREATEST(edge_sync_sequence.last_value, EXCLUDED.last_value);

-- 3. Estado do fluxo de envio por loja (backoff + último cursor confirmado pela nuvem)
CREATE TABLE IF NOT EXISTS edge_sync_stream_state (
    store_id               UUID        PRIMARY KEY REFERENCES store(id) ON DELETE CASCADE,
    cloud_ack_sequence     BIGINT      NOT NULL DEFAULT 0,
    consecutive_failures   INTEGER     NOT NULL DEFAULT 0 CHECK (consecutive_failures >= 0),
    next_attempt_at        TIMESTAMP,
    last_success_at        TIMESTAMP,
    last_error             TEXT,
    full_resync_requested  BOOLEAN     NOT NULL DEFAULT FALSE,
    updated_at             TIMESTAMP   NOT NULL DEFAULT now()
);

-- ── LADO NUVEM ─────────────────────────────────────────────────────────────

-- 4. Log de idempotência passa a registrar sequência e resultado (APPLIED | REJECTED)
ALTER TABLE edge_synced_event_log
    ADD COLUMN IF NOT EXISTS sequence        BIGINT,
    ADD COLUMN IF NOT EXISTS outcome         VARCHAR(20) NOT NULL DEFAULT 'APPLIED'
        CHECK (outcome IN ('APPLIED', 'REJECTED')),
    ADD COLUMN IF NOT EXISTS rejected_reason TEXT;

CREATE INDEX IF NOT EXISTS idx_edge_synced_log_store_seq
    ON edge_synced_event_log (store_id, sequence)
    WHERE sequence IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_edge_synced_log_purge
    ON edge_synced_event_log (received_at);

-- 5. Cursor de sequência contígua por loja na nuvem
CREATE TABLE IF NOT EXISTS edge_sync_cursor (
    store_id                  UUID       PRIMARY KEY REFERENCES store(id) ON DELETE CASCADE,
    last_contiguous_sequence  BIGINT     NOT NULL CHECK (last_contiguous_sequence >= 0),
    initialized_at            TIMESTAMP  NOT NULL DEFAULT now(),
    updated_at                TIMESTAMP  NOT NULL DEFAULT now()
);

