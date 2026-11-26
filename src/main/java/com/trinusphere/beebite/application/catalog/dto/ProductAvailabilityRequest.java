package com.trinusphere.beebite.application.catalog.dto;

import jakarta.validation.constraints.NotNull;

/**
 * @param scope {@code CATALOG_GLOBAL} (padrão): pausa/ativa no Cardápio inteiro (todas as lojas + iFood).
 *              {@code STORE_ONLY}: altera apenas a disponibilidade local da unidade atual (não afeta o iFood).
 */
public record ProductAvailabilityRequest(
        @NotNull(message = "status é obrigatório") String status,
        @NotNull(message = "availableDigitalMenu é obrigatório") Boolean availableDigitalMenu,
        @NotNull(message = "availablePdv é obrigatório") Boolean availablePdv,
        String scope) {

    public static final String SCOPE_STORE_ONLY = "STORE_ONLY";
    public static final String SCOPE_CATALOG_GLOBAL = "CATALOG_GLOBAL";

    public ProductAvailabilityRequest(String status, Boolean availableDigitalMenu, Boolean availablePdv) {
        this(status, availableDigitalMenu, availablePdv, SCOPE_CATALOG_GLOBAL);
    }

    public boolean isStoreOnly() {
        return SCOPE_STORE_ONLY.equalsIgnoreCase(scope);
    }
}
