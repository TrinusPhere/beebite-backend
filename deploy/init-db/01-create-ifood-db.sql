-- =============================================================================
-- Cria o segundo banco lógico (biteflow_ifood) dentro da mesma instância
-- PostgreSQL 17 para economizar memória RAM na VM e2-micro.
-- =============================================================================
CREATE DATABASE biteflow_ifood;
GRANT ALL PRIVILEGES ON DATABASE biteflow_ifood TO CURRENT_USER;

