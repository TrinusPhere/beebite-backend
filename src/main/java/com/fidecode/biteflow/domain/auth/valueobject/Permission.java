package com.fidecode.biteflow.domain.auth.valueobject;

import java.util.Arrays;

public enum Permission {
    DASHBOARD("dashboard"),
    PEDIDOS("pedidos"),
    CLIENTES("clientes"),
    INGREDIENTES("ingredientes"),
    PRODUTOS("produtos"),
    ENTREGADORES("entregadores"),
    ACERTO_ENTREGADORES("acerto_entregadores"),
    USUARIOS("usuarios"),
    INTEGRACOES("integracoes"),
    FINANCEIRO("financeiro");

    private final String value;

    Permission(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Permission fromValue(String value) {
        return Arrays.stream(values())
            .filter(p -> p.value.equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Permissão inválida: " + value));
    }
}
