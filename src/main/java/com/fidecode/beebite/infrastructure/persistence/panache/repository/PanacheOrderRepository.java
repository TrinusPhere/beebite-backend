package com.fidecode.beebite.infrastructure.persistence.panache.repository;

import com.fidecode.beebite.domain.order.entity.*;
import com.fidecode.beebite.domain.order.repository.OrderRepository;
import com.fidecode.beebite.domain.order.valueobject.OrderStatus;
import com.fidecode.beebite.domain.shared.exception.ConcurrentModificationException;
import com.fidecode.beebite.infrastructure.persistence.panache.entity.*;
import com.fidecode.beebite.infrastructure.persistence.panache.mapper.OrderMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheOrderRepository implements OrderRepository {

    @Inject
    OrderPanacheRepo repo;
    @Inject
    EntityManager em;
    @Inject
    com.fidecode.beebite.application.sync.EnqueueEdgeOutboxUseCase enqueueEdgeOutboxUseCase;

    @Override
    @Transactional
    public Optional<Order> findById(UUID id) {
        return repo.findByIdOptional(id).map(this::toDomainFull);
    }

    @Override
    @Transactional
    public com.fidecode.beebite.domain.shared.PageResult<Order> findAllByStoreId(UUID storeId, int page, int size) {
        var query = repo.find("storeId", storeId).page(io.quarkus.panache.common.Page.of(page, size));
        List<Order> content = query.list().stream().map(this::toDomainFull).toList();
        return new com.fidecode.beebite.domain.shared.PageResult<>(content, page, size, query.count(),
                query.pageCount());
    }

    @Override
    @Transactional
    public com.fidecode.beebite.domain.shared.PageResult<Order> findByStoreIdAndStatus(UUID storeId,
            OrderStatus status, int page, int size) {
        var query = repo.find("storeId = ?1 and status = ?2", storeId, status.name())
                .page(io.quarkus.panache.common.Page.of(page, size));
        List<Order> content = query.list().stream().map(this::toDomainFull).toList();
        return new com.fidecode.beebite.domain.shared.PageResult<>(content, page, size, query.count(),
                query.pageCount());
    }

    @Override
    @Transactional
    public List<Order> findActiveKanbanOrdersByStoreId(UUID storeId, java.time.LocalDateTime startOfDay) {
        return repo.find(
                "SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.storeId = ?1 and o.createdAt >= ?2",
                storeId, startOfDay).list().stream().map(this::toDomainFull).toList();
    }

    @Override
    @Transactional
    public List<Order> findByStoreIdAndDateRange(UUID storeId, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return repo.find(
                "SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.storeId = ?1 AND o.createdAt >= ?2 AND o.createdAt <= ?3 ORDER BY o.createdAt DESC",
                storeId, start, end).list().stream().map(this::toDomainFull).toList();
    }

    @Override
    @Transactional
    public List<Order> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        return repo.find("id in ?1", ids).list().stream().map(this::toDomainFull).toList();
    }

    @Override
    @Transactional
    public Optional<Order> findByExternalOrderId(String externalOrderId) {
        return repo.findByExternalOrderId(externalOrderId).map(this::toDomainFull);
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity = OrderMapper.toEntity(order);
        try {
            if (entity.id == null) {
                repo.persist(entity);
            } else {
                entity = em.merge(entity);
            }
            Order savedDomain = toDomainFull(entity);
            if (enqueueEdgeOutboxUseCase != null && savedDomain.getId() != null) {
                enqueueEdgeOutboxUseCase.execute(
                        savedDomain.getStoreId(),
                        "ORDER_UPSERT",
                        savedDomain.getId(),
                        buildOrderOutboxJson(entity)
                );
            }
            return savedDomain;
        } catch (jakarta.persistence.OptimisticLockException | org.hibernate.StaleObjectStateException ex) {
            // Converte exceção de infra para exceção de domínio — mantém P7
            String id = entity.id != null ? entity.id.toString() : "desconhecido";
            throw new ConcurrentModificationException("pedido", id);
        }
    }

    private String buildOrderOutboxJson(OrderEntity e) {
        return String.format(
                java.util.Locale.US,
                "{\"displayId\":\"%s\",\"type\":\"%s\",\"status\":\"%s\",\"paymentMethod\":\"%s\",\"platform\":\"%s\",\"operation\":\"%s\",\"channel\":\"%s\",\"serviceType\":\"%s\",\"source\":\"%s\",\"subtotal\":%s,\"deliveryCost\":%s,\"totalAmount\":%s}",
                e.displayId != null ? e.displayId : "0",
                e.type != null ? e.type : "BALCAO",
                e.status != null ? e.status : "NOVO",
                e.paymentMethod != null ? e.paymentMethod : "DINHEIRO",
                e.platform != null ? e.platform : "LOCAL",
                e.operation != null ? e.operation : "BALCAO",
                e.channel != null ? e.channel : "PDV",
                e.serviceType != null ? e.serviceType : "COUNTER",
                e.source != null ? e.source : "EDGE_PDV",
                e.subtotal != null ? e.subtotal.toPlainString() : "0.00",
                e.deliveryCost != null ? e.deliveryCost.toPlainString() : "0.00",
                e.totalValue != null ? e.totalValue.toPlainString() : "0.00"
        );
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repo.findByIdOptional(id).ifPresent(entity -> {
            entity.deletedAt = java.time.LocalDateTime.now();
            em.merge(entity);
        });
    }

    @Override
    public long getNextDisplayIdSequence() {
        return ((Number) em.createNativeQuery("SELECT nextval('order_display_id_seq')").getSingleResult()).longValue();
    }

    @Override
    @Transactional
    public void resetDisplayIdSequence() {
        em.createNativeQuery("ALTER SEQUENCE order_display_id_seq RESTART WITH 1001").executeUpdate();
    }

    private Order toDomainFull(OrderEntity e) {
        List<OrderItem> items = new ArrayList<>();

        if (e.items != null) {
            for (OrderItemEntity itemEntity : e.items) {
                List<OrderItemModifier> modifiers = itemEntity.modifiers != null
                        ? itemEntity.modifiers.stream().map(OrderMapper::toModifierDomain).toList() : new ArrayList<>();

                List<OrderItemRemoval> removals = itemEntity.removals != null
                        ? itemEntity.removals.stream().map(OrderMapper::toRemovalDomain).toList() : new ArrayList<>();

                items.add(OrderMapper.toItemDomain(itemEntity, modifiers, removals));
            }
        }

        return OrderMapper.toDomain(e, items);
    }
}
