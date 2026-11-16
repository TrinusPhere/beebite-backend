package com.trinusphere.beebite.domain.payment;

import com.trinusphere.beebite.domain.order.valueobject.PaymentMethod;
import com.trinusphere.beebite.domain.payment.entity.Payment;
import com.trinusphere.beebite.domain.payment.valueobject.PaymentCollector;
import com.trinusphere.beebite.domain.payment.valueobject.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment — Regras de Domínio")
class PaymentTest {

    @Test
    @DisplayName("Deve criar pagamento no caixa com status CONFIRMED")
    void shouldCreatePaymentAtCashRegister() {
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("45.50");

        Payment payment = Payment.createAtRegister(
                storeId, orderId, sessionId, PaymentMethod.CREDITO, amount, null, null
        );

        assertNotNull(payment);
        assertEquals(storeId, payment.getStoreId());
        assertEquals(orderId, payment.getOrderId());
        assertEquals(sessionId, payment.getCashSessionId());
        assertNull(payment.getDriverId());
        assertEquals(PaymentMethod.CREDITO, payment.getMethod());
        assertEquals(amount, payment.getAmount());
        assertEquals(PaymentCollector.CAIXA, payment.getCollectedBy());
        assertEquals(PaymentStatus.CONFIRMED, payment.getStatus());
        assertNotNull(payment.getPaidAt());
    }

    @Test
    @DisplayName("Deve criar pagamento recebido pelo entregador com motorista associado")
    void shouldCreatePaymentCollectedByDriver() {
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("70.00");
        BigDecimal changeFor = new BigDecimal("100.00");

        Payment payment = Payment.createByDriver(
                storeId, orderId, sessionId, driverId, PaymentMethod.DINHEIRO, amount, changeFor, "Troco para 100"
        );

        assertNotNull(payment);
        assertEquals(driverId, payment.getDriverId());
        assertEquals(PaymentCollector.ENTREGADOR, payment.getCollectedBy());
        assertEquals(PaymentMethod.DINHEIRO, payment.getMethod());
        assertEquals(changeFor, payment.getChangeFor());
        assertEquals("Troco para 100", payment.getNotes());
    }

    @Test
    @DisplayName("Deve cancelar pagamento")
    void shouldCancelPayment() {
        UUID storeId = UUID.randomUUID();
        Payment payment = Payment.createAtRegister(
                storeId, UUID.randomUUID(), UUID.randomUUID(), PaymentMethod.PIX, new BigDecimal("20.00"), null, null
        );

        assertEquals(PaymentStatus.CONFIRMED, payment.getStatus());
        payment.cancel();
        assertEquals(PaymentStatus.CANCELED, payment.getStatus());
    }
}
