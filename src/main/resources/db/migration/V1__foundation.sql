-- ═══════════════════════════════════════════════════
-- V1__foundation.sql — BiteFlow Fase 1: Store + Users
-- ═══════════════════════════════════════════════════

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Store (loja) ──────────────────────────────────
CREATE TABLE store (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    logo_url        VARCHAR(500),
    is_open         BOOLEAN      NOT NULL DEFAULT false,
    opening_hours   VARCHAR(255),
    address         VARCHAR(500),
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

-- ── Users ─────────────────────────────────────────
CREATE TABLE users (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID        NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    login           VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','GERENTE','CAIXA')),
    permissions     TEXT[]       NOT NULL DEFAULT '{}',
    active          BOOLEAN      NOT NULL DEFAULT true,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_store_id   ON users(store_id);
CREATE INDEX idx_users_login      ON users(login);
CREATE INDEX idx_users_active     ON users(active);

-- ── Seed: loja e usuário admin padrão ─────────────
INSERT INTO store (id, name, logo_url, is_open, opening_hours, address)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'BiteFlow Lanchonete',
    null,
    false,
    'Seg-Sex: 11:00 - 23:00 | Sáb-Dom: 12:00 - 00:00',
    'Rua da Lanchonete, 123 - Centro'
);

-- Senha: admin123 (BCrypt hash)
INSERT INTO users (id, store_id, name, login, password_hash, role, permissions, active)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000001',
    'Administrador',
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    ARRAY['dashboard','pedidos','clientes','ingredientes','produtos','entregadores','acerto_entregadores','usuarios','integracoes','financeiro'],
    true
);
