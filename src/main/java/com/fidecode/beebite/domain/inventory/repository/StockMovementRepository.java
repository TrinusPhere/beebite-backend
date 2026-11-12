package com.fidecode.beebite.domain.inventory.repository;

import com.fidecode.beebite.domain.inventory.entity.StockMovement;
import java.util.*;

public interface StockMovementRepository {
    List<StockMovement> findAllByStoreId(UUID storeId);

    StockMovement save(StockMovement movement);
}
