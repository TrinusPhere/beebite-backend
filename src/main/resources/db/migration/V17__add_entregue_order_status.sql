-- Migration V17: Adiciona status ENTREGUE no check constraint da tabela orders
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;
ALTER TABLE orders ADD CONSTRAINT orders_status_check CHECK (status::text = ANY (ARRAY['ANALISE', 'PREPARO', 'EM_ROTA', 'ENTREGUE', 'FINALIZADO', 'CANCELADO']));

