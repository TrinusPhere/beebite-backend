-- V8__unique_soft_deletes.sql
-- Ajusta as restrições de unicidade para ignorar registros com deleted_at preenchido

-- Remover a constraint UNIQUE da coluna login
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_login_key;

-- Criar um índice parcial UNIQUE para garantir unicidade apenas em usuários não deletados
CREATE UNIQUE INDEX idx_users_login_unique ON users(login) WHERE deleted_at IS NULL;
