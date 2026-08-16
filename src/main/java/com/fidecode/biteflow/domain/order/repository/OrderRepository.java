package com.fidecode.biteflow.domain.order.repository;

import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port — interface do repositório de pedidos.
 */
public interface OrderRepository {

    Optional<Order> findById(UUID id);

    List<Order> findAllByStoreId(UUID storeId);

    List<Order> findByStoreIdAndStatus(UUID storeId, OrderStatus status);

    Order save(Order order);

    void deleteById(UUID id);

    long getNextDisplayIdSequence();
}
