package com.fidecode.biteflow.application.order;

import com.fidecode.biteflow.application.order.dto.OrderResponse;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.valueobject.OrderPlatform;
import com.fidecode.biteflow.domain.order.valueobject.OrderStatus;
import com.fidecode.biteflow.domain.order.valueobject.OrderType;
import com.fidecode.biteflow.domain.order.valueobject.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrderHistoryUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private GetOrderHistoryUseCase useCase;

    private static final UUID STORE_ID = UUID.randomUUID();

    @Test
    @DisplayName("Deve retornar histórico de pedidos para a data informada")
    void deveRetornarHistoricoDePedidos() {
        LocalDate date = LocalDate.of(2026, 11, 6);
        LocalDateTime now = date.atTime(19, 30);

        Order order = Order.reconstitute(
                UUID.randomUUID(), STORE_ID, "1001", UUID.randomUUID(), "Maria Silva",
                "Rua A, 100", OrderType.DELIVERY, OrderStatus.FINALIZADO,
                PaymentMethod.PIX, UUID.randomUUID(), "Carlos", OrderPlatform.OWN,
                null, new BigDecimal("50.00"), new BigDecimal("5.00"), new BigDecimal("55.00"),
                false, List.of(), now, now, now
        );

        when(orderRepository.findByStoreIdAndDateRange(eq(STORE_ID), any(), any()))
                .thenReturn(List.of(order));

        List<OrderResponse> result = useCase.execute(STORE_ID, date);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1001", result.get(0).displayId());
        assertEquals("Maria Silva", result.get(0).customerName());
        assertEquals("FINALIZADO", result.get(0).status());
    }
}
