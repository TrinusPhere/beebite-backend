-- =============================================================================
-- Cria o segundo banco lógico (beebite_ifood) dentro da mesma instância
-- PostgreSQL 17 para economizar memória RAM na VM e2-micro.
-- =============================================================================
CREATE DATABASE beebite_ifood;
GRANT ALL PRIVILEGES ON DATABASE beebite_ifood TO CURRENT_USER;

