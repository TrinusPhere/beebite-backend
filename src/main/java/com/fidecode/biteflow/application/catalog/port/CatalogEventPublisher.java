package com.fidecode.biteflow.application.catalog.port;

import java.util.UUID;

public interface CatalogEventPublisher {

    void publishCatalogChanged(UUID storeId, UUID productId, String changeType);

}
