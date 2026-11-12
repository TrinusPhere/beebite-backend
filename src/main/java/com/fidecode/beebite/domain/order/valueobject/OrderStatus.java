package com.fidecode.beebite.domain.order.valueobject;

/**
 * Status do pedido — 4 colunas do Kanban + CANCELADO.
 */
public enum OrderStatus {
    ANALISE, PREPARO, EM_ROTA, FINALIZADO, CANCELADO
}
