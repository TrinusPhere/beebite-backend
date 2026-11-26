package com.trinusphere.beebite.application.platform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @param catalogId      opcional — vincula a nova loja a um Cardápio existente do cliente
 * @param newCatalogName opcional — se informado, cria um novo Cardápio independente (ex.: 2ª marca do cliente)
 *                       e vincula a loja a ele. Se ambos forem nulos, usa o Cardápio padrão do cliente.
 */
public record CreateStoreForTenantRequest(
        @NotBlank(message = "O nome da loja é obrigatório.") String name,
        String cnpj,
        String address,
        String publicSlug,
        @NotBlank(message = "O modo de sincronização é obrigatório.") String syncMode,
        String ownerLoginToLink,
        String catalogId,
        String newCatalogName
) {
    public CreateStoreForTenantRequest(
            String name,
            String cnpj,
            String address,
            String publicSlug,
            String syncMode,
            String ownerLoginToLink) {
        this(name, cnpj, address, publicSlug, syncMode, ownerLoginToLink, null, null);
    }
}
