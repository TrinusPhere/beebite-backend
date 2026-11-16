package com.trinusphere.beebite.domain.printing.valueobject;

import java.util.Arrays;

/** Tipos de documento imprimíveis. Cada tipo possui seu próprio modelo de comanda. */
public enum DocumentType {
    KITCHEN, EXPEDITION, CUSTOMER_RECEIPT;

    public static DocumentType fromValue(String value) {
        if (value == null)
            throw new IllegalArgumentException("Tipo de documento é obrigatório.");
        return Arrays.stream(values()).filter(t -> t.name().equalsIgnoreCase(value.trim())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de documento inválido: " + value));
    }
}

