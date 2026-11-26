package com.trinusphere.beebite.application.sync.port;

import java.util.UUID;

/**
 * Port para inicialização automática do banco local em nós Edge (PDV Offline-First).
 */
public interface EdgeStoreBootstrapPort {

    void bootstrapClientStoreAndCleanSeed(
            UUID edgeStoreId,
            UUID edgeCatalogId,
            String storeName,
            String publicSlug,
            String address,
            String adminLogin,
            UUID defaultSeedStoreId
    );

    void ensureLocalAdminIsStoreAdmin(
            UUID edgeStoreId,
            String adminLogin,
            String adminDisplayName
    );
}
