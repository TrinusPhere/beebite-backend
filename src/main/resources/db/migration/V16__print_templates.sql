-- V16__print_templates.sql
-- Impressão térmica (ESC/POS): configuração da bobina por impressora e modelos de comanda por tipo de documento.

ALTER TABLE printer
    ADD COLUMN paper_width_mm SMALLINT    NOT NULL DEFAULT 80,
    ADD COLUMN port           INTEGER     NOT NULL DEFAULT 9100,
    ADD COLUMN code_page      VARCHAR(20) NOT NULL DEFAULT 'CP860',
    ADD COLUMN cut_paper      BOOLEAN     NOT NULL DEFAULT true;

ALTER TABLE printer
    ADD CONSTRAINT chk_printer_paper_width CHECK (paper_width_mm IN (58, 80)),
    ADD CONSTRAINT chk_printer_port CHECK (port BETWEEN 1 AND 65535);

CREATE TABLE print_template (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id       UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    document_type  VARCHAR(30) NOT NULL,
    sections       JSONB NOT NULL DEFAULT '{}'::jsonb,
    header_text    VARCHAR(255),
    footer_text    VARCHAR(255),
    large_title    BOOLEAN NOT NULL DEFAULT true,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_print_template_type CHECK (document_type IN ('KITCHEN', 'EXPEDITION', 'CUSTOMER_RECEIPT')),
    CONSTRAINT uq_print_template_store_type UNIQUE (store_id, document_type)
);
CREATE INDEX idx_print_template_store ON print_template(store_id);

