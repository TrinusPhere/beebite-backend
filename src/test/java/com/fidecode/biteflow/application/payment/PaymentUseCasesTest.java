package com.fidecode.biteflow.application.payment;

import com.fidecode.biteflow.application.payment.dto.PaymentRequest;
import com.fidecode.biteflow.application.payment.dto.PaymentResponse;
import com.fidecode.biteflow.domain.order.entity.Order;
import com.fidecode.biteflow.domain.order.repository.OrderRepository;
import com.fidecode.biteflow.domain.order.valueobject.OrderType;
import com.fidecode.biteflow.domain.order.valueobject.PaymentMethod;
import com.fidecode.biteflow.domain.payment.entity.Payment;
import com.fidecode.biteflow.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Use Cases")
class PaymentUseCasesTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Test
    @DisplayName("RegisterPaymentUseCase deve registrar pagamento com entregador associado")
    void shouldRegisterDriverPayment() {
        RegisterPaymentUseCase useCase = new RegisterPaymentUseCase(paymentRepository, orderRepository);
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        Order order = new Order(storeId, OrderType.DELIVERY, PaymentMethod.DINHEIRO);
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PaymentRequest req = new PaymentRequest(
                orderId.toString(), sessionId.toString(), driverId.toString(),
                "DINHEIRO", new BigDecimal("50.00"), new BigDecimal("100.00"), "ENTREGADOR", "Pago ao motoboy"
        );

        PaymentResponse res = useCase.execute(storeId, req);

        assertNotNull(res);
        assertEquals("DINHEIRO", res.method());
        assertEquals(new BigDecimal("50.00"), res.amount());
        assertEquals(driverId.toString(), res.driverId());
        assertEquals("ENTREGADOR", res.collectedBy());
        verify(paymentRepository).save(any(Payment.class));
    }
}
