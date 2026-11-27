package com.trinusphere.beebite.infrastructure.sync;

import com.trinusphere.beebite.application.sync.port.EdgeOutboxRequeuePort;
import com.trinusphere.beebite.application.sync.port.EdgeReconciliationDigestPort;
import com.trinusphere.beebite.application.sync.port.EdgeSnapshotApplier;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.entity.OrderItem;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import com.trinusphere.beebite.domain.order.valueobject.OrderStatus;
import com.trinusphere.beebite.domain.order.valueobject.OrderType;
import com.trinusphere.beebite.domain.order.valueobject.PaymentMethod;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.domain.sync.valueobject.ReconciliationEntityType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class NativeSqlReconciliationDigestAndRequeueIT {

    @Inject
    StoreRepository storeRepository;
    @Inject
    OrderRepository orderRepository;
    @Inject
    EdgeSyncPayloadSerializer serializer;
    @Inject
    EdgeSnapshotApplier snapshotApplier;
    @Inject
    EdgeReconciliationDigestPort digestPort;
    @Inject
    EdgeOutboxRequeuePort requeuePort;

    @Test
    void shouldProduceIdenticalFingerprintOnEdgeAndCloudAndRequeueAggregate() {
        Store edgeStore = storeRepository.save(new Store("Edge Parity Store"));
        Store cloudStore = storeRepository.save(new Store("Cloud Parity Store"));

        Order order = new Order(edgeStore.getId(), OrderType.BALCAO, PaymentMethod.PIX);
        order.setDisplayId("#9001");
        OrderItem item = new OrderItem(UUID.randomUUID(), "Smash", 2, new BigDecimal("25.00"), null);
        item.setId(UUID.randomUUID());
        order.addItem(item);
        order.changeStatus(OrderStatus.FINALIZADO);
        order = orderRepository.save(order);

        String payload = serializer.serializeOrder(order);
        snapshotApplier.apply(cloudStore.getId(), "ORDER_UPSERT", order.getId(), payload);

        LocalDate bucketDate = order.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
        var edgeItems = digestPort.computeItemFingerprints(edgeStore.getId(), ReconciliationEntityType.ORDER, bucketDate);
        var cloudItems = digestPort.computeItemFingerprints(cloudStore.getId(), ReconciliationEntityType.ORDER, bucketDate);

        assertEquals(1, edgeItems.size());
        assertEquals(1, cloudItems.size());
        assertEquals(edgeItems.get(0).fingerprint(), cloudItems.get(0).fingerprint());
    }
}

