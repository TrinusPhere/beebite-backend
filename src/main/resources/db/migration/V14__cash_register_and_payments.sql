-- ═══════════════════════════════════════════════════════════════════════════
-- V14__cash_register_and_payments.sql
-- Módulo de Controle de Caixa, Sessões de Operador, Movimentações (Sangria,
-- Suprimento, Ajuste Compensatório) e Entidade Desacoplada de Pagamentos.
-- ═══════════════════════════════════════════════════════════════════════════

-- 1. Cadastro de Caixas Físicos / PDV da Loja
CREATE TABLE IF NOT EXISTS cash_register (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cash_register_store ON cash_register(store_id);

-- 2. Sessão de Caixa (Abertura, Operação e Fechamento)
CREATE TABLE IF NOT EXISTS cash_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    cash_register_id UUID NOT NULL REFERENCES cash_register(id) ON DELETE RESTRICT,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- 'OPEN', 'CLOSED'
    opened_at TIMESTAMP NOT NULL DEFAULT now(),
    closed_at TIMESTAMP,
    initial_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    expected_amount DECIMAL(12,2),
    counted_amount DECIMAL(12,2),
    difference_amount DECIMAL(12,2),
    closing_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Regra: Apenas uma sessão ABERTA por usuário na loja
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_open_session_per_user 
    ON cash_session(store_id, user_id) WHERE status = 'OPEN';

CREATE INDEX IF NOT EXISTS idx_cash_session_store_status ON cash_session(store_id, status);
CREATE INDEX IF NOT EXISTS idx_cash_session_register ON cash_session(cash_register_id);

-- 3. Movimentações de Caixa (Sangria, Suprimento e Ajuste Compensatório)
CREATE TABLE IF NOT EXISTS cash_movement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    cash_session_id UUID NOT NULL REFERENCES cash_session(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    type VARCHAR(30) NOT NULL, -- 'SUPRIMENTO', 'SANGRIA', 'AJUSTE_COMPENSATORIO'
    amount DECIMAL(12,2) NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_cash_movement_session ON cash_movement(cash_session_id);
CREATE INDEX IF NOT EXISTS idx_cash_movement_store ON cash_movement(store_id);

-- 4. Entidade Desacoplada de Pagamentos (com Entregador e Meio de Pagamento)
CREATE TABLE IF NOT EXISTS payment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    order_id UUID REFERENCES orders(id) ON DELETE SET NULL,
    cash_session_id UUID REFERENCES cash_session(id) ON DELETE SET NULL,
    driver_id UUID REFERENCES delivery_driver(id) ON DELETE SET NULL,
    method VARCHAR(30) NOT NULL, -- 'DINHEIRO', 'DEBITO', 'CREDITO', 'PIX', 'OUTRO'
    amount DECIMAL(12,2) NOT NULL,
    change_for DECIMAL(12,2),
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED', -- 'PENDING', 'CONFIRMED', 'CANCELED'
    collected_by VARCHAR(30) NOT NULL DEFAULT 'CAIXA', -- 'CAIXA', 'ENTREGADOR', 'ONLINE'
    notes TEXT,
    paid_at TIMESTAMP NOT NULL DEFAULT now(),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_payment_store_paid_at ON payment(store_id, paid_at);
CREATE INDEX IF NOT EXISTS idx_payment_order ON payment(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_session ON payment(cash_session_id);
CREATE INDEX IF NOT EXISTS idx_payment_driver ON payment(driver_id);
