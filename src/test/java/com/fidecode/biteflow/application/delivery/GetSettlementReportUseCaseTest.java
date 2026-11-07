package com.fidecode.biteflow.application.delivery;

import com.fidecode.biteflow.application.delivery.dto.SettlementReportResponse;
import com.fidecode.biteflow.domain.delivery.entity.DeliveryDriver;
import com.fidecode.biteflow.domain.delivery.repository.DeliveryDriverRepository;
import com.fidecode.biteflow.domain.delivery.valueobject.DriverStatus;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.valueobject.OrderPlatform;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import com.fidecode.biteflow.domain.order.valueobject.OrderType;
import com.fidecode.biteflow.domain.order.valueobject.PaymentMethod;
import com.fidecode.biteflow.domain.shared.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSettlementReportUseCaseTest {

    @Mock
    private DeliveryDriverRepository driverRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private GetSettlementReportUseCase useCase;

    private static final UUID STORE_ID = UUID.randomUUID();

    @Test
    @DisplayName("Deve calcular relatório de acerto com entregas e números dos pedidos")
    void deveCalcularRelatorioComPedidos() {
        UUID driver1Id = UUID.randomUUID();
        DeliveryDriver driver1 = DeliveryDriver.reconstitute(
                driver1Id, STORE_ID, "Carlos Motoboy", "11999998888",
                DriverStatus.ATIVO, true, false,
                new BigDecimal("50.00"), // allowance (ajuda de custo)
                true, new BigDecimal("5.00"), // taxa por entrega
                10, BigDecimal.valueOf(4.8), LocalDateTime.now(), LocalDateTime.now()
        );

        UUID driver2Id = UUID.randomUUID();
        DeliveryDriver driver2 = DeliveryDriver.reconstitute(
                driver2Id, STORE_ID, "João Diarista", "11977776666",
                DriverStatus.ATIVO, false, true,
                new BigDecimal("30.00"),
                true, new BigDecimal("4.00"),
                5, BigDecimal.valueOf(5.0), LocalDateTime.now(), LocalDateTime.now()
        );

        when(driverRepository.findAllByStoreId(eq(STORE_ID), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(driver1, driver2), 0, 1000, 2, 1));

        LocalDateTime now = LocalDateTime.now();

        // Pedido 1 entregue por Carlos
        Order order1 = Order.reconstitute(
                UUID.randomUUID(), STORE_ID, "1001", UUID.randomUUID(), "Cliente A",
                "Rua das Flores, 123", OrderType.DELIVERY, OrderStatus.FINALIZADO,
                PaymentMethod.PIX, driver1Id, "Carlos Motoboy", OrderPlatform.OWN,
                null, new BigDecimal("60.00"), new BigDecimal("5.00"), new BigDecimal("65.00"),
                false, List.of(), now.minusHours(2), now.minusHours(1), now.minusHours(1)
        );

        // Pedido 2 entregue por Carlos
        Order order2 = Order.reconstitute(
                UUID.randomUUID(), STORE_ID, "1002", UUID.randomUUID(), "Cliente B",
                "Av Central, 456", OrderType.DELIVERY, OrderStatus.FINALIZADO,
                PaymentMethod.DINHEIRO, driver1Id, "Carlos Motoboy", OrderPlatform.OWN,
                null, new BigDecimal("40.00"), new BigDecimal("5.00"), new BigDecimal("45.00"),
                false, List.of(), now.minusHours(1), now, now
        );

        // Pedido 3 cancelado (não deve contar nas entregas válidas)
        Order order3 = Order.reconstitute(
                UUID.randomUUID(), STORE_ID, "1003", UUID.randomUUID(), "Cliente C",
                "Rua X", OrderType.DELIVERY, OrderStatus.CANCELADO,
                PaymentMethod.CREDITO, driver1Id, "Carlos Motoboy", OrderPlatform.OWN,
                null, new BigDecimal("50.00"), new BigDecimal("5.00"), new BigDecimal("55.00"),
                false, List.of(), now.minusHours(3), now, null
        );

        when(orderRepository.findByStoreIdAndDateRange(eq(STORE_ID), any(), any()))
                .thenReturn(List.of(order1, order2, order3));

        SettlementReportResponse response = useCase.execute(STORE_ID, "daily", null);

        assertNotNull(response);
        assertEquals(2, response.entries().size());

        // Carlos: 2 entregas válidas * 5.00 = 10.00 + 50.00 allowance = 60.00
        var entry1 = response.entries().stream().filter(e -> e.driverId().equals(driver1Id.toString())).findFirst().orElseThrow();
        assertEquals(2, entry1.deliveries());
        assertEquals(new BigDecimal("5.00"), entry1.feePerDelivery());
        assertEquals(new BigDecimal("50.00"), entry1.allowance());
        assertEquals(new BigDecimal("60.00"), entry1.total());
        assertEquals(2, entry1.orders().size());
        assertEquals(1001L, entry1.orders().get(0).displayId());
        assertEquals("Cliente A", entry1.orders().get(0).customerName());

        // João: 0 entregas * 4.00 = 0.00 + 30.00 allowance = 30.00
        var entry2 = response.entries().stream().filter(e -> e.driverId().equals(driver2Id.toString())).findFirst().orElseThrow();
        assertEquals(0, entry2.deliveries());
        assertEquals(new BigDecimal("30.00"), entry2.total());
        assertTrue(entry2.orders().isEmpty());

        // Total geral = 60.00 + 30.00 = 90.00
        assertEquals(new BigDecimal("90.00"), response.grandTotal());
    }
}
