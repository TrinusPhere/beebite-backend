package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.order.entity.*;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import com.fidecode.biteflow.domain.shared.exception.ConcurrentModificationException;
import com.fidecode.biteflow.infrastructure.persistence.panache.entity.*;
import com.fidecode.biteflow.infrastructure.persistence.panache.mapper.OrderMapper;
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

    @Override
    @Transactional
    public Optional<Order> findById(UUID id) {
        return repo.findByIdOptional(id).map(this::toDomainFull);
    }

    @Override
    @Transactional
    public com.fidecode.biteflow.domain.shared.PageResult<Order> findAllByStoreId(UUID storeId, int page, int size) {
        var query = repo.find("storeId", storeId).page(io.quarkus.panache.common.Page.of(page, size));
        List<Order> content = query.list().stream().map(this::toDomainFull).toList();
        return new com.fidecode.biteflow.domain.shared.PageResult<>(content, page, size, query.count(),
                query.pageCount());
    }

    @Override
    @Transactional
    public com.fidecode.biteflow.domain.shared.PageResult<Order> findByStoreIdAndStatus(UUID storeId,
            OrderStatus status, int page, int size) {
        var query = repo.find("storeId = ?1 and status = ?2", storeId, status.name())
                .page(io.quarkus.panache.common.Page.of(page, size));
        List<Order> content = query.list().stream().map(this::toDomainFull).toList();
        return new com.fidecode.biteflow.domain.shared.PageResult<>(content, page, size, query.count(),
                query.pageCount());
    }

    @Override
    @Transactional
    public List<Order> findActiveKanbanOrdersByStoreId(UUID storeId, java.time.LocalDateTime startOfDay) {
        return repo.find(
                "SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.storeId = ?1 and (o.status != 'FINALIZADO' or o.createdAt >= ?2)",
                storeId, startOfDay).list().stream().map(this::toDomainFull).toList();
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
            return toDomainFull(entity);
        } catch (jakarta.persistence.OptimisticLockException | org.hibernate.StaleObjectStateException ex) {
            // Converte exceção de infra para exceção de domínio — mantém P7
            String id = entity.id != null ? entity.id.toString() : "desconhecido";
            throw new ConcurrentModificationException("pedido", id);
        }
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
