-- V7__soft_deletes.sql
-- Adiciona a coluna deleted_at em todas as tabelas principais (Aggregate Roots)

ALTER TABLE store ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE ingredient ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE product ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE modifier_group ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE additional ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE customer ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE orders ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE delivery_driver ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE product_category ADD COLUMN deleted_at TIMESTAMP;

-- Adicionar índices para otimizar as queries com "deleted_at IS NULL"
CREATE INDEX idx_store_deleted_at ON store(deleted_at);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
CREATE INDEX idx_ingredient_deleted_at ON ingredient(deleted_at);
CREATE INDEX idx_product_deleted_at ON product(deleted_at);
CREATE INDEX idx_modifier_group_deleted_at ON modifier_group(deleted_at);
CREATE INDEX idx_additional_deleted_at ON additional(deleted_at);
CREATE INDEX idx_customer_deleted_at ON customer(deleted_at);
CREATE INDEX idx_orders_deleted_at ON orders(deleted_at);
CREATE INDEX idx_delivery_driver_deleted_at ON delivery_driver(deleted_at);
CREATE INDEX idx_product_category_deleted_at ON product_category(deleted_at);
