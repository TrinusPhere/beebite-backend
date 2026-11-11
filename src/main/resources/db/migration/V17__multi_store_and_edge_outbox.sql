-- ═══════════════════════════════════════════════════════════════════════════
-- V17__multi_store_and_edge_outbox.sql
-- 1. Acesso Multi-Loja Genérico (User <-> N Stores sem deslogar)
-- 2. Sincronização Híbrida Edge -> Cloud (Transactional Outbox + Idempotência)
-- ═══════════════════════════════════════════════════════════════════════════

-- ── 1. Vínculo N:N Usuário <-> Lojas (Multi-Store Access) ─────────────────
CREATE TABLE user_store_access (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    store_id    UUID        NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    role        VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN','GERENTE','CAIXA')),
    permissions TEXT[]      NOT NULL DEFAULT '{}',
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_store_access UNIQUE (user_id, store_id)
);

CREATE INDEX idx_user_store_access_user ON user_store_access(user_id);
CREATE INDEX idx_user_store_access_store ON user_store_access(store_id);

-- Backfill automático: garante que todo usuário existente já tenha acesso à sua store_id original
INSERT INTO user_store_access (user_id, store_id, role, permissions)
SELECT id, store_id, role, permissions FROM users
ON CONFLICT (user_id, store_id) DO NOTHING;

-- ── 2. Configuração Edge na tabela Store ──────────────────────────────────
ALTER TABLE store
    ADD COLUMN IF NOT EXISTS sync_mode VARCHAR(20) NOT NULL DEFAULT 'CLOUD'
        CHECK (sync_mode IN ('CLOUD', 'EDGE')),
    ADD COLUMN IF NOT EXISTS edge_api_key_hash VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_edge_sync_at TIMESTAMP;

-- ── 3. Fila Local de Saída (Transactional Outbox no Nó Edge) ──────────────
CREATE TABLE edge_sync_outbox (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID         NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    event_type      VARCHAR(60)  NOT NULL,
    aggregate_id    UUID         NOT NULL,
    payload         JSONB        NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'SYNCED', 'FAILED')),
    attempts        INTEGER      NOT NULL DEFAULT 0,
    last_error      TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    synced_at       TIMESTAMP
);

CREATE INDEX idx_edge_outbox_pending
    ON edge_sync_outbox(status, created_at ASC)
    WHERE status = 'PENDING';

-- ── 4. Controle de Idempotência na Nuvem (Evita aplicar 2x o mesmo evento) ─
CREATE TABLE edge_synced_event_log (
    event_id        UUID         PRIMARY KEY,
    store_id        UUID         NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    event_type      VARCHAR(60)  NOT NULL,
    aggregate_id    UUID         NOT NULL,
    received_at     TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_edge_synced_log_store ON edge_synced_event_log(store_id, received_at DESC);
