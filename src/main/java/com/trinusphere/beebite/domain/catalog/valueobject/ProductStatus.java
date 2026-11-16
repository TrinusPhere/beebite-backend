package com.trinusphere.beebite.domain.catalog.valueobject;

public enum ProductStatus {
    AVAILABLE,    // Disponível para venda
    UNAVAILABLE,  // Indisponível (desativado pelo lojista)
    PAUSED        // Pausado temporariamente (ex: ingrediente acabou, volta amanhã)
}
