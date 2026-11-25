package com.trinusphere.beebite.infrastructure.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trinusphere.beebite.application.sync.BackfillEdgeOutboxUseCase;
import com.trinusphere.beebite.application.sync.port.EdgeOutboxBackfillPort;
import com.trinusphere.beebite.domain.cash.entity.CashMovement;
import com.trinusphere.beebite.domain.cash.entity.CashRegister;
import com.trinusphere.beebite.domain.cash.entity.CashSession;
import com.trinusphere.beebite.domain.cash.valueobject.MovementType;
import com.trinusphere.beebite.domain.financial.entity.FinancialRecord;
import com.trinusphere.beebite.domain.financial.valueobject.FinancialRecordProduct;
import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.entity.OrderItem;
import com.trinusphere.beebite.domain.order.valueobject.OrderStatus;
import com.trinusphere.beebite.domain.order.valueobject.OrderType;
import com.trinusphere.beebite.domain.order.valueobject.PaymentMethod;
import com.trinusphere.beebite.domain.payment.entity.Payment;
import com.trinusphere.beebite.domain.store.entity.Store;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EdgeOperationalSyncTest {

    @Mock
    EntityManager entityManager;

    @Mock
    Query nativeQuery;

    @Mock
    EdgeOutboxBackfillPort backfillPort;

    private ObjectMapper objectMapper;
    private EdgeSyncPayloadSerializer serializer;
    private PanacheEdgeSnapshotApplier applier;

    private static final UUID STORE_ID = UUID.fromString("e72810a1-afe4-43f1-8dc9-71054a010216");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        serializer = new EdgeSyncPayloadSerializer(objectMapper);
        applier = new PanacheEdgeSnapshotApplier(entityManager, objectMapper);
    }

    @Test
    void shouldSerializeOrderWithItemsAndSpecialCharactersSafely() throws Exception {
        Order order = new Order(STORE_ID, OrderType.BALCAO, PaymentMethod.PIX);
        order.setId(UUID.randomUUID());
        order.setDisplayId("#1005");
        order.updateCustomerInfo("João \"Chef\" D'Ávila\nMesa 2", "Balcão");
        OrderItem item = new OrderItem(UUID.randomUUID(), "X-Bacon Duplo", 2, new BigDecimal("32.50"), "Sem cebola");
        item.setId(UUID.randomUUID());
        order.addItem(item);
        order.changeStatus(OrderStatus.FINALIZADO);

        String json = serializer.serializeOrder(order);
        JsonNode root = objectMapper.readTree(json);

        assertEquals("#1005", root.get("displayId").asText());
        assertEquals("FINALIZADO", root.get("status").asText());
        assertEquals("PIX", root.get("paymentMethod").asText());
        assertEquals(0, new BigDecimal("65.00").compareTo(new BigDecimal(root.get("totalAmount").asText())));
        assertNotNull(root.get("finishedAt"));
        assertEquals(1, root.get("items").size());
        assertEquals("X-Bacon Duplo", root.get("items").get(0).get("productName").asText());
        assertEquals(2, root.get("items").get(0).get("quantity").asInt());
    }

    @Test
    void shouldSerializeFinancialCashAndPaymentEventsCompletely() throws Exception {
        UUID orderId = UUID.randomUUID();
        FinancialRecord fr = new FinancialRecord(
                STORE_ID,
                orderId,
                new BigDecimal("65.00"),
                PaymentMethod.PIX,
                List.of(new FinancialRecordProduct(UUID.randomUUID(), "X-Bacon Duplo", 2, new BigDecimal("32.50")))
        );
        fr.setId(UUID.randomUUID());

        JsonNode frNode = objectMapper.readTree(serializer.serializeFinancialRecord(fr));
        assertEquals(orderId.toString(), frNode.get("orderId").asText());
        assertEquals("CONCLUIDO", frNode.get("status").asText());
        assertEquals(1, frNode.get("products").size());

        CashRegister register = new CashRegister(STORE_ID, "Caixa 01");
        register.setId(UUID.randomUUID());
        JsonNode regNode = objectMapper.readTree(serializer.serializeCashRegister(register));
        assertEquals("Caixa 01", regNode.get("name").asText());
        assertTrue(regNode.get("active").asBoolean());

        CashSession session = new CashSession(STORE_ID, register.getId(), UUID.randomUUID(), new BigDecimal("100.00"));
        session.setId(UUID.randomUUID());
        JsonNode sessNode = objectMapper.readTree(serializer.serializeCashSession(session));
        assertEquals("OPEN", sessNode.get("status").asText());
        assertEquals(0, new BigDecimal("100.00").compareTo(new BigDecimal(sessNode.get("initialAmount").asText())));

        CashMovement movement = new CashMovement(STORE_ID, session.getId(), session.getUserId(),
                MovementType.SUPRIMENTO, new BigDecimal("50.00"), "Troco extra");
        movement.setId(UUID.randomUUID());
        JsonNode movNode = objectMapper.readTree(serializer.serializeCashMovement(movement));
        assertEquals("SUPRIMENTO", movNode.get("type").asText());

        Payment payment = Payment.createAtRegister(STORE_ID, orderId, session.getId(),
                PaymentMethod.PIX, new BigDecimal("65.00"), null, "Pago no QR");
        payment.setId(UUID.randomUUID());
        JsonNode payNode = objectMapper.readTree(serializer.serializePayment(payment));
        assertEquals("CONFIRMED", payNode.get("status").asText());
        assertEquals("PIX", payNode.get("method").asText());

        Store store = new Store("Chefs Burguer");
        store.setId(STORE_ID);
        store.open();
        JsonNode storeNode = objectMapper.readTree(serializer.serializeStoreStatus(store));
        assertTrue(storeNode.get("open").asBoolean());
    }

    @Test
    void shouldThrowExceptionInsteadOfSwallowingWhenPayloadJsonIsCorrupted() {
        assertThrows(IllegalStateException.class, () ->
                applier.apply(STORE_ID, "ORDER_UPSERT", UUID.randomUUID(), "{corrupted-json"));
    }

    @Test
    void shouldApplyLegacyMinimalOrderPayloadAndFullOrderWithItems() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of());
        when(nativeQuery.executeUpdate()).thenReturn(1);

        UUID legacyOrderId = UUID.randomUUID();
        String legacyPayload = "{\"displayId\":\"#1001\",\"status\":\"FINALIZADO\",\"type\":\"BALCAO\",\"totalAmount\":\"42.90\",\"paymentMethod\":\"DINHEIRO\"}";

        assertDoesNotThrow(() -> applier.apply(STORE_ID, "ORDER_UPSERT", legacyOrderId, legacyPayload));

        Order fullOrder = new Order(STORE_ID, OrderType.BALCAO, PaymentMethod.PIX);
        fullOrder.setId(UUID.randomUUID());
        fullOrder.setDisplayId("#1002");
        OrderItem item = new OrderItem(UUID.randomUUID(), "Smash Burguer", 1, new BigDecimal("29.90"), null);
        item.setId(UUID.randomUUID());
        fullOrder.addItem(item);
        fullOrder.changeStatus(OrderStatus.FINALIZADO);

        assertDoesNotThrow(() -> applier.apply(STORE_ID, "ORDER_UPSERT", fullOrder.getId(), serializer.serializeOrder(fullOrder)));
        verify(entityManager, atLeast(3)).createNativeQuery(anyString());
    }

    @Test
    void shouldApplyCashSessionAndFinancialRecordResolvingMissingForeignKeys() {
        UUID cloudOwnerUserId = UUID.randomUUID();
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of(cloudOwnerUserId));
        when(nativeQuery.executeUpdate()).thenReturn(1);

        UUID registerId = UUID.randomUUID();
        UUID edgeUserId = UUID.randomUUID();
        CashSession session = new CashSession(STORE_ID, registerId, edgeUserId, new BigDecimal("200.00"));
        session.setId(UUID.randomUUID());

        assertDoesNotThrow(() -> applier.apply(
                STORE_ID, "CASH_SESSION_UPSERT", session.getId(), serializer.serializeCashSession(session)));

        UUID orderId = UUID.randomUUID();
        FinancialRecord record = new FinancialRecord(
                STORE_ID,
                orderId,
                new BigDecimal("49.90"),
                PaymentMethod.CREDITO,
                List.of(new FinancialRecordProduct(UUID.randomUUID(), "Combo Casal", 1, new BigDecimal("49.90")))
        );
        record.setId(UUID.randomUUID());

        assertDoesNotThrow(() -> applier.apply(
                STORE_ID, "FINANCIAL_RECORD_UPSERT", record.getId(), serializer.serializeFinancialRecord(record)));
    }

    @Test
    void shouldBackfillMissingEventsOnlyWhenInEdgeMode() {
        BackfillEdgeOutboxUseCase edgeUseCase = new BackfillEdgeOutboxUseCase(
                backfillPort, "EDGE", Optional.of(STORE_ID.toString()));
        when(backfillPort.backfillMissingEvents(STORE_ID)).thenReturn(5);

        assertEquals(5, edgeUseCase.execute());
        verify(backfillPort).backfillMissingEvents(STORE_ID);

        reset(backfillPort);
        BackfillEdgeOutboxUseCase cloudUseCase = new BackfillEdgeOutboxUseCase(
                backfillPort, "CLOUD", Optional.of(STORE_ID.toString()));
        assertEquals(0, cloudUseCase.execute());
        verifyNoInteractions(backfillPort);
    }
}
