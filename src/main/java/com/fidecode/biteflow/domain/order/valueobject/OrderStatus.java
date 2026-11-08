package com.fidecode.biteflow.domain.order.valueobject;

/**
 * Status do pedido — 5 colunas do Kanban + CANCELADO.
 */
public enum OrderStatus {
    ANALISE, PREPARO, EM_ROTA, ENTREGUE, FINALIZADO, CANCELADO
}
