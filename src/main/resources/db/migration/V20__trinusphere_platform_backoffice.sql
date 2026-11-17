-- ═══════════════════════════════════════════════════════════════════════════
-- V20__trinusphere_platform_backoffice.sql
-- Backoffice SaaS da Trinusphere:
-- 1. Papel SUPER_ADMIN para o administrador global da Trinusphere
-- 2. Tabela tenant_account (Clientes contratantes da Trinusphere)
-- 3. Vínculo store -> tenant_account + CNPJ por loja + active
-- 4. Tabela saas_contract (Contratos e planos de assinatura)
-- 5. Tabela saas_invoice (Mensalidades, taxas de implantação e contas a receber)
-- ═══════════════════════════════════════════════════════════════════════════

-- ── 1. Permitir role SUPER_ADMIN em users e user_store_access ────────────
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users
    ADD CONSTRAINT users_role_check
        CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'GERENTE', 'CAIXA'));

ALTER TABLE user_store_access
    DROP CONSTRAINT IF EXISTS user_store_access_role_check;

ALTER TABLE user_store_access
    ADD CONSTRAINT user_store_access_role_check
        CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'GERENTE', 'CAIXA'));

-- Promove o usuário 'admin' original para SUPER_ADMIN da Trinusphere
UPDATE users
SET role = 'SUPER_ADMIN',
    name = 'Trinusphere Super Admin'
WHERE login = 'admin';

-- ── 2. Tabela de Clientes da Trinusphere (Tenant Account) ─────────────────
CREATE TABLE tenant_account (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name        VARCHAR(150)    NOT NULL,
    trade_name          VARCHAR(150)    NOT NULL,
    owner_name          VARCHAR(120)    NOT NULL,
    document_number     VARCHAR(20),
    email               VARCHAR(150),
    phone               VARCHAR(30),
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('TRIAL', 'ACTIVE', 'SUSPENDED', 'CANCELLED')),
    notes               TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_tenant_account_status ON tenant_account(status);

-- Cria o Tenant padrão da própria Trinusphere para a loja matriz inicial
INSERT INTO tenant_account (id, company_name, trade_name, owner_name, document_number, email, status)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Trinusphere Tecnologia Ltda',
    'Trinusphere Matriz / Demo',
    'Admin Trinusphere',
    '00.000.000/0001-00',
    'admin@trinusphere.com.br',
    'ACTIVE'
)
ON CONFLICT (id) DO NOTHING;

-- ── 3. Evolução da tabela store (vínculo com Tenant + CNPJ próprio) ───────
ALTER TABLE store
    ADD COLUMN IF NOT EXISTS tenant_id UUID REFERENCES tenant_account(id),
    ADD COLUMN IF NOT EXISTS cnpj VARCHAR(20),
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true;

UPDATE store
SET tenant_id = '00000000-0000-0000-0000-000000000001'
WHERE tenant_id IS NULL;

CREATE INDEX idx_store_tenant_id ON store(tenant_id);

-- ── 4. Tabela de Contratos SaaS da Trinusphere ────────────────────────────
CREATE TABLE saas_contract (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID            NOT NULL REFERENCES tenant_account(id) ON DELETE CASCADE,
    plan_name           VARCHAR(80)     NOT NULL,
    billing_cycle       VARCHAR(20)     NOT NULL DEFAULT 'MONTHLY'
        CHECK (billing_cycle IN ('MONTHLY', 'QUARTERLY', 'YEARLY')),
    monthly_fee         NUMERIC(12,2)   NOT NULL DEFAULT 0.00,
    setup_fee           NUMERIC(12,2)   NOT NULL DEFAULT 0.00,
    max_stores          INTEGER         NOT NULL DEFAULT 1,
    includes_ifood      BOOLEAN         NOT NULL DEFAULT true,
    includes_edge_sync  BOOLEAN         NOT NULL DEFAULT false,
    billing_day         INTEGER         NOT NULL DEFAULT 10 CHECK (billing_day BETWEEN 1 AND 28),
    start_date          DATE            NOT NULL DEFAULT CURRENT_DATE,
    end_date            DATE,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('TRIAL', 'ACTIVE', 'SUSPENDED', 'CANCELLED')),
    contract_notes      TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_saas_contract_tenant ON saas_contract(tenant_id);
CREATE INDEX idx_saas_contract_status ON saas_contract(status);

-- ── 5. Tabela de Faturas / Mensalidades SaaS (Financeiro Trinusphere) ─────
CREATE TABLE saas_invoice (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id         UUID            NOT NULL REFERENCES saas_contract(id) ON DELETE CASCADE,
    tenant_id           UUID            NOT NULL REFERENCES tenant_account(id) ON DELETE CASCADE,
    reference_month     VARCHAR(7)      NOT NULL,
    description         VARCHAR(200)    NOT NULL,
    amount              NUMERIC(12,2)   NOT NULL,
    due_date            DATE            NOT NULL,
    paid_at             TIMESTAMP,
    payment_method      VARCHAR(30),
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED')),
    notes               TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_saas_invoice_tenant ON saas_invoice(tenant_id);
CREATE INDEX idx_saas_invoice_status_due ON saas_invoice(status, due_date);

