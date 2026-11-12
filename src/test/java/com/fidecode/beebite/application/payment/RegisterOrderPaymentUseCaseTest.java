package com.fidecode.beebite.application.payment;

import com.fidecode.beebite.domain.cash.entity.CashSession;
import com.fidecode.beebite.domain.cash.repository.CashSessionRepository;
import com.fidecode.beebite.domain.cash.valueobject.CashSessionStatus;
import com.fidecode.beebite.domain.financial.event.OrderFinalizedEvent;
import com.fidecode.beebite.domain.order.entity.Order;
import com.fidecode.beebite.domain.order.repository.OrderRepository;
import com.fidecode.beebite.domain.order.valueobject.PaymentMethod;
import com.fidecode.beebite.domain.payment.entity.Payment;
import com.fidecode.beebite.domain.payment.repository.PaymentRepository;
import com.fidecode.beebite.domain.payment.valueobject.PaymentCollector;
import com.fidecode.beebite.domain.payment.valueobject.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterOrderPaymentUseCase Tests")
class RegisterOrderPaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CashSessionRepository cashSessionRepository;

    @Mock
    private OrderRepository orderRepository;

    private RegisterOrderPaymentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterOrderPaymentUseCase(paymentRepository, cashSessionRepository, orderRepository);
    }

    @Test
    @DisplayName("Deve registrar pagamento vinculado à sessão aberta quando pedido for finalizado sem pagamentos prévios")
    void shouldRegisterPaymentLinkedToOpenCashSession() {
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        CashSession session = new CashSession(storeId, UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100.00"));
        session.setId(sessionId);

        when(cashSessionRepository.findByStoreIdAndStatus(storeId, CashSessionStatus.OPEN))
                .thenReturn(List.of(session));
        when(paymentRepository.findByOrderId(orderId)).thenReturn(List.of());

        Order order = mock(Order.class);
        when(order.getDisplayId()).thenReturn("1005");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderFinalizedEvent event = new OrderFinalizedEvent(
                orderId, storeId, new BigDecimal("47.90"), PaymentMethod.CREDITO,
                UUID.randomUUID(), driverId, List.of()
        );

        useCase.execute(event);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());

        Payment saved = captor.getValue();
        assertEquals(storeId, saved.getStoreId());
        assertEquals(orderId, saved.getOrderId());
        assertEquals(sessionId, saved.getCashSessionId());
        assertEquals(driverId, saved.getDriverId());
        assertEquals(PaymentMethod.CREDITO, saved.getMethod());
        assertEquals(new BigDecimal("47.90"), saved.getAmount());
        assertEquals(PaymentCollector.ENTREGADOR, saved.getCollectedBy());
        assertEquals(PaymentStatus.CONFIRMED, saved.getStatus());
        assertTrue(saved.getNotes().contains("1005"));
    }

    @Test
    @DisplayName("Deve vincular pagamento existente à sessão aberta caso ainda não esteja vinculado")
    void shouldLinkExistingPaymentToOpenCashSession() {
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        CashSession session = new CashSession(storeId, UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100.00"));
        session.setId(sessionId);

        Payment existingPayment = new Payment(
                storeId, orderId, null, null, PaymentMethod.PIX,
                new BigDecimal("35.00"), null, PaymentCollector.CAIXA, "Manual"
        );

        when(cashSessionRepository.findByStoreIdAndStatus(storeId, CashSessionStatus.OPEN))
                .thenReturn(List.of(session));
        when(paymentRepository.findByOrderId(orderId)).thenReturn(List.of(existingPayment));

        OrderFinalizedEvent event = new OrderFinalizedEvent(
                orderId, storeId, new BigDecimal("35.00"), PaymentMethod.PIX,
                null, null, List.of()
        );

        useCase.execute(event);

        verify(paymentRepository).save(existingPayment);
        assertEquals(sessionId, existingPayment.getCashSessionId());
    }

    @Test
    @DisplayName("Deve registrar pagamento mesmo se não houver sessão de caixa aberta no momento")
    void shouldRegisterPaymentEvenIfNoCashSessionIsOpen() {
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(cashSessionRepository.findByStoreIdAndStatus(storeId, CashSessionStatus.OPEN))
                .thenReturn(List.of());
        when(paymentRepository.findByOrderId(orderId)).thenReturn(List.of());
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderFinalizedEvent event = new OrderFinalizedEvent(
                orderId, storeId, new BigDecimal("20.00"), PaymentMethod.DINHEIRO,
                null, null, List.of()
        );

        useCase.execute(event);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());

        Payment saved = captor.getValue();
        assertNull(saved.getCashSessionId());
        assertEquals(PaymentMethod.DINHEIRO, saved.getMethod());
        assertEquals(PaymentCollector.CAIXA, saved.getCollectedBy());
    }
}

