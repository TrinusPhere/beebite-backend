package com.fidecode.beebite.domain.catalog.valueobject;

public enum ModifierGroupSemanticType {
    OFFER_UNIT,    // Venda cruzada/adicional (ex: "Escolha uma bebida")
    SPECIFICATION, // Especificação de preparo (ex: "Ponto da carne")
    INGREDIENTS,   // Alteração de ingredientes (ex: "Sem cebola")
    CUTLERY        // Utensílios (ex: "Precisa de talher?")
}
