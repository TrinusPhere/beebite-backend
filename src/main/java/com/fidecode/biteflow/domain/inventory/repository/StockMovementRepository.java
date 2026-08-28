package com.fidecode.biteflow.domain.inventory.repository;

import com.fidecode.biteflow.domain.inventory.entity.StockMovement;
import java.util.*;

public interface StockMovementRepository {
    List<StockMovement> findAllByStoreId(UUID storeId);

    StockMovement save(StockMovement movement);
}
