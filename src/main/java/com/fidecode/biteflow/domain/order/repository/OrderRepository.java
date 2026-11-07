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

    com.fidecode.biteflow.domain.shared.PageResult<Order> findAllByStoreId(UUID storeId, int page, int size);

    com.fidecode.biteflow.domain.shared.PageResult<Order> findByStoreIdAndStatus(UUID storeId, OrderStatus status,
            int page, int size);

    Order save(Order order);

    void deleteById(UUID id);

    long getNextDisplayIdSequence();

    void resetDisplayIdSequence();

    List<Order> findActiveKanbanOrdersByStoreId(UUID storeId, java.time.LocalDateTime startOfDay);

    List<Order> findByStoreIdAndDateRange(UUID storeId, java.time.LocalDateTime start, java.time.LocalDateTime end);

    List<Order> findAllById(List<UUID> ids);

    /** Busca pedido pelo ID externo (ex: ID do pedido no iFood). Usado na integração iFood. */
    Optional<Order> findByExternalOrderId(String externalOrderId);
}
