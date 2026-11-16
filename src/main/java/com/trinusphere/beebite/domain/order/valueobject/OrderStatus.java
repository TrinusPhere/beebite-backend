package com.trinusphere.beebite.domain.order.valueobject;

/**
 * Status do pedido — 5 colunas do Kanban + CANCELADO.
 */
public enum OrderStatus {
    ANALISE, PREPARO, EM_ROTA, ENTREGUE, FINALIZADO, CANCELADO
}
