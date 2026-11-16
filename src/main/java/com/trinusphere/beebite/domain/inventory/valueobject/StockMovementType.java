package com.trinusphere.beebite.domain.inventory.valueobject;

public enum StockMovementType {
    ENTRADA, SAIDA, PERDA, QUEBRA, VENDA, COMPRA, DEVOLUCAO, AJUSTE, TRANSFERENCIA, PRODUCAO, INVENTARIO;

    public boolean isOutbound() {
        return this == SAIDA || this == PERDA || this == QUEBRA || this == VENDA;
    }
}
