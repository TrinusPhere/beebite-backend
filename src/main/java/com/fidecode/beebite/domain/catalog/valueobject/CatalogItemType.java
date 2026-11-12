package com.fidecode.beebite.domain.catalog.valueobject;

public enum CatalogItemType {
    DEFAULT,   // Item simples (lanche, bebida, etc.)
    PIZZA,     // Estrutura especial com grupos SIZE, CRUST, EDGE, TOPPING
    COMBO      // Bundle de produtos com optionGroups MAIN
}
