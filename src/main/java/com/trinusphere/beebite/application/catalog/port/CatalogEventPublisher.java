package com.trinusphere.beebite.application.catalog.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface CatalogEventPublisher {

    /**
     * @param catalogId           Cardápio dono do produto (escopo do vínculo com o iFood)
     * @param storeId             loja que originou a alteração
     * @param effectiveIfoodPrice preço a enviar ao iFood (canal IFOOD ou preço base); pode ser nulo em exclusões
     * @param globalStatus        status GLOBAL do produto no Cardápio (AVAILABLE/UNAVAILABLE); pode ser nulo em exclusões
     */
    void publishCatalogChanged(UUID catalogId, UUID storeId, UUID productId, String changeType,
            BigDecimal effectiveIfoodPrice, String globalStatus);

}
