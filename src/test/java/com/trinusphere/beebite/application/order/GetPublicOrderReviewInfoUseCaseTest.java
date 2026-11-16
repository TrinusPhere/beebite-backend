package com.trinusphere.beebite.application.order;

import com.trinusphere.beebite.domain.order.entity.Order;
import com.trinusphere.beebite.domain.order.repository.OrderRepository;
import com.trinusphere.beebite.domain.order.valueobject.OrderType;
import com.trinusphere.beebite.domain.order.valueobject.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPublicOrderReviewInfoUseCaseTest {

    @Mock
    OrderRepository repository;

    @Test
    void deveRetornarNumeroDoPedidoENomeRealDoEntregador() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(UUID.randomUUID(), OrderType.DELIVERY, PaymentMethod.PIX);
        order.setId(orderId);
        order.setDisplayId("1002");
        order.assignDriver(UUID.randomUUID(), "João Entregador");
        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        var response = new GetPublicOrderReviewInfoUseCase(repository).execute(orderId);

        assertEquals("1002", response.displayId());
        assertEquals("João Entregador", response.driverName());
    }
}
