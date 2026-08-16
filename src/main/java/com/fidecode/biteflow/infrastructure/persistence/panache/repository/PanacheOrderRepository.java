package com.fidecode.biteflow.infrastructure.persistence.panache.repository;

import com.fidecode.biteflow.domain.order.entity.*;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
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

    @Inject OrderPanacheRepo repo;
    @Inject EntityManager em;

    @Override
    public Optional<Order> findById(UUID id) {
        return repo.findByIdOptional(id).map(this::toDomainFull);
    }

    @Override
    public List<Order> findAllByStoreId(UUID storeId) {
        return repo.find("storeId", storeId).list().stream()
            .map(this::toDomainFull).toList();
    }

    @Override
    public List<Order> findByStoreIdAndStatus(UUID storeId, OrderStatus status) {
        return repo.find("storeId = ?1 and status = ?2", storeId, status.name()).list().stream()
            .map(this::toDomainFull).toList();
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderEntity entity = OrderMapper.toEntity(order);

        if (entity.id == null) {
            repo.persist(entity);
        } else {
            entity = em.merge(entity);
        }

        UUID orderId = entity.id;

        // Delete existing items and re-insert
        // First delete modifiers and removals for existing items
        List<OrderItemEntity> existingItems = em.createQuery(
            "SELECT i FROM OrderItemEntity i WHERE i.orderId = :oid", OrderItemEntity.class)
            .setParameter("oid", orderId).getResultList();

        for (OrderItemEntity existingItem : existingItems) {
            em.createQuery("DELETE FROM OrderItemModifierEntity WHERE orderItemId = :iid")
                .setParameter("iid", existingItem.id).executeUpdate();
            em.createQuery("DELETE FROM OrderItemRemovalEntity WHERE orderItemId = :iid")
                .setParameter("iid", existingItem.id).executeUpdate();
        }
        em.createQuery("DELETE FROM OrderItemEntity WHERE orderId = :oid")
            .setParameter("oid", orderId).executeUpdate();

        // Insert items with modifiers and removals
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                OrderItemEntity itemEntity = OrderMapper.toItemEntity(item, orderId);
                if (itemEntity.id == null) {
                    em.persist(itemEntity);
                } else {
                    itemEntity = em.merge(itemEntity);
                }

                UUID itemId = itemEntity.id;

                if (item.getModifiers() != null) {
                    for (OrderItemModifier mod : item.getModifiers()) {
                        OrderItemModifierEntity modEntity = OrderMapper.toModifierEntity(mod, itemId);
                        em.persist(modEntity);
                    }
                }

                if (item.getRemovals() != null) {
                    for (OrderItemRemoval rem : item.getRemovals()) {
                        OrderItemRemovalEntity remEntity = OrderMapper.toRemovalEntity(rem, itemId);
                        em.persist(remEntity);
                    }
                }
            }
        }

        return toDomainFull(entity);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        // Delete child entities first
        List<OrderItemEntity> items = em.createQuery(
            "SELECT i FROM OrderItemEntity i WHERE i.orderId = :oid", OrderItemEntity.class)
            .setParameter("oid", id).getResultList();

        for (OrderItemEntity item : items) {
            em.createQuery("DELETE FROM OrderItemModifierEntity WHERE orderItemId = :iid")
                .setParameter("iid", item.id).executeUpdate();
            em.createQuery("DELETE FROM OrderItemRemovalEntity WHERE orderItemId = :iid")
                .setParameter("iid", item.id).executeUpdate();
        }
        em.createQuery("DELETE FROM OrderItemEntity WHERE orderId = :oid")
            .setParameter("oid", id).executeUpdate();
        repo.deleteById(id);
    }

    @Override
    public long getNextDisplayIdSequence() {
        return ((Number) em.createNativeQuery("SELECT nextval('order_display_id_seq')")
            .getSingleResult()).longValue();
    }

    private Order toDomainFull(OrderEntity e) {
        List<OrderItemEntity> itemEntities = em.createQuery(
            "SELECT i FROM OrderItemEntity i WHERE i.orderId = :oid", OrderItemEntity.class)
            .setParameter("oid", e.id).getResultList();

        List<OrderItem> items = new ArrayList<>();
        for (OrderItemEntity itemEntity : itemEntities) {
            List<OrderItemModifierEntity> modEntities = em.createQuery(
                "SELECT m FROM OrderItemModifierEntity m WHERE m.orderItemId = :iid",
                OrderItemModifierEntity.class)
                .setParameter("iid", itemEntity.id).getResultList();

            List<OrderItemRemovalEntity> remEntities = em.createQuery(
                "SELECT r FROM OrderItemRemovalEntity r WHERE r.orderItemId = :iid",
                OrderItemRemovalEntity.class)
                .setParameter("iid", itemEntity.id).getResultList();

            List<OrderItemModifier> modifiers = modEntities.stream()
                .map(OrderMapper::toModifierDomain).toList();
            List<OrderItemRemoval> removals = remEntities.stream()
                .map(OrderMapper::toRemovalDomain).toList();

            items.add(OrderMapper.toItemDomain(itemEntity, modifiers, removals));
        }

        return OrderMapper.toDomain(e, items);
    }
}
