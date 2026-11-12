-- V18: Rebranding default store name from BiteFlow to BeeBite
UPDATE store
SET name = 'BeeBite Lanchonete',
    updated_at = CURRENT_TIMESTAMP
WHERE id = '00000000-0000-0000-0000-000000000001'
  AND name = 'BiteFlow Lanchonete';

