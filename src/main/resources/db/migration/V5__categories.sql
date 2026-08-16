CREATE TABLE product_category (
    id UUID PRIMARY KEY,
    store_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    default_modifier_group_ids TEXT,
    created_at TIMESTAMP NOT NULL
);

-- Inserir as categorias existentes a partir dos produtos
INSERT INTO product_category (id, store_id, name, created_at)
SELECT gen_random_uuid(), store_id, category, NOW()
FROM product
WHERE category IS NOT NULL AND category != ''
GROUP BY store_id, category;

-- Adicionar o novo campo category_id em product
ALTER TABLE product ADD COLUMN category_id UUID;

-- Atualizar os produtos com os IDs das novas categorias
UPDATE product p
SET category_id = pc.id
FROM product_category pc
WHERE p.category = pc.name AND p.store_id = pc.store_id;

-- Remover a antiga coluna
ALTER TABLE product DROP COLUMN category;

-- Criar constraint opcional para integridade
ALTER TABLE product
    ADD CONSTRAINT fk_product_category
    FOREIGN KEY (category_id)
    REFERENCES product_category (id)
    ON DELETE SET NULL;
