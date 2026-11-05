-- ═══════════════════════════════════════════════════════════════════════════
-- V13__kds_module.sql
-- Módulo KDS (Kitchen Display System), Estações de Trabalho, Tarefas de Produção
-- e Componentes de Combos para Direcionamento por Setores.
-- ═══════════════════════════════════════════════════════════════════════════

-- 1. Suporte a COMBO no tipo de produto
ALTER TABLE product DROP CONSTRAINT IF EXISTS chk_product_type;
ALTER TABLE product ADD CONSTRAINT chk_product_type CHECK (product_type IN ('PRODUCED', 'RESALE', 'COMBO'));

-- 2. Tabela de Componentes de Combos
CREATE TABLE IF NOT EXISTS product_combo_component (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    combo_product_id UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    component_product_id UUID NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_combo_component_combo ON product_combo_component(combo_product_id);
CREATE INDEX IF NOT EXISTS idx_combo_component_store ON product_combo_component(store_id);

-- 3. Estações KDS (telas de monitor por setor ou expedição consolidada)
CREATE TABLE IF NOT EXISTS kds_station (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    is_expedition BOOLEAN NOT NULL DEFAULT false,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_kds_station_store ON kds_station(store_id);

-- 4. Associação Estação KDS <-> Setores de Produção
CREATE TABLE IF NOT EXISTS kds_station_sector (
    station_id UUID NOT NULL REFERENCES kds_station(id) ON DELETE CASCADE,
    sector_id UUID NOT NULL REFERENCES production_sector(id) ON DELETE CASCADE,
    PRIMARY KEY (station_id, sector_id)
);

-- 5. Configurações de KDS por Loja
CREATE TABLE IF NOT EXISTS kds_settings (
    store_id UUID PRIMARY KEY REFERENCES store(id) ON DELETE CASCADE,
    dispatch_mode VARCHAR(20) NOT NULL DEFAULT 'AUTO', -- 'AUTO' ou 'MANUAL'
    warning_alert_minutes INT NOT NULL DEFAULT 10,
    critical_alert_minutes INT NOT NULL DEFAULT 20,
    auto_complete_on_expedition BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 6. Tarefas de Produção KDS
CREATE TABLE IF NOT EXISTS production_task (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    order_item_id UUID NOT NULL REFERENCES order_item(id) ON DELETE CASCADE,
    sector_id UUID REFERENCES production_sector(id) ON DELETE SET NULL,
    display_order_id VARCHAR(50),
    order_type VARCHAR(30),
    order_channel VARCHAR(40),
    customer_name VARCHAR(150),
    item_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    notes TEXT,
    modifiers_summary TEXT,
    combo_name VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'IN_PREPARATION', 'READY', 'CANCELED'
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    canceled_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_production_task_store_status ON production_task(store_id, status);
CREATE INDEX IF NOT EXISTS idx_production_task_order ON production_task(order_id);
CREATE INDEX IF NOT EXISTS idx_production_task_sector ON production_task(sector_id);

