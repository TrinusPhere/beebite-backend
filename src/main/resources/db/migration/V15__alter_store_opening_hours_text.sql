-- V15__alter_store_opening_hours_text.sql
-- Altera a coluna opening_hours de VARCHAR(255) para TEXT para suportar o JSON completo da grade semanal de horários da loja.
-- Define a loja como aberta por padrão.

ALTER TABLE store ALTER COLUMN opening_hours TYPE TEXT;
UPDATE store SET is_open = true WHERE deleted_at IS NULL;
