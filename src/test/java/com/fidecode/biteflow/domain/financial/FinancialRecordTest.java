package com.fidecode.biteflow.domain.financial;

import com.fidecode.biteflow.domain.financial.entity.FinancialRecord;
import com.fidecode.biteflow.domain.financial.valueobject.FinancialRecordProduct;
import com.fidecode.biteflow.domain.financial.valueobject.FinancialStatus;
import com.fidecode.biteflow.domain.order.valueobject.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FinancialRecordTest {

    private static final UUID STORE_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();

    @Test
    void deveCriarRegistroFinanceiroComStatusConcluido() {
        var products = List.of(
            new FinancialRecordProduct(UUID.randomUUID(), "X-Bacon", 2, new BigDecimal("35.00"))
        );

        var record = new FinancialRecord(STORE_ID, ORDER_ID,
            new BigDecimal("70.00"), PaymentMethod.PIX, products);

        assertEquals(STORE_ID, record.getStoreId());
        assertEquals(ORDER_ID, record.getOrderId());
        assertEquals(new BigDecimal("70.00"), record.getTotalValue());
        assertEquals(PaymentMethod.PIX, record.getPaymentMethod());
        assertEquals(FinancialStatus.CONCLUIDO, record.getStatus());
        assertNotNull(record.getRecordDate());
        assertNotNull(record.getRecordTime());
        assertNotNull(record.getCreatedAt());
        assertEquals(1, record.getProducts().size());
    }

    @Test
    void deveLancarExcecaoQuandoStoreIdNulo() {
        assertThrows(IllegalArgumentException.class, () ->
            new FinancialRecord(null, ORDER_ID, BigDecimal.TEN, PaymentMethod.PIX, null));
    }

    @Test
    void deveLancarExcecaoQuandoOrderIdNulo() {
        assertThrows(IllegalArgumentException.class, () ->
            new FinancialRecord(STORE_ID, null, BigDecimal.TEN, PaymentMethod.PIX, null));
    }

    @Test
    void deveLancarExcecaoQuandoTotalValueNulo() {
        assertThrows(IllegalArgumentException.class, () ->
            new FinancialRecord(STORE_ID, ORDER_ID, null, PaymentMethod.PIX, null));
    }

    @Test
    void deveLancarExcecaoQuandoPaymentMethodNulo() {
        assertThrows(IllegalArgumentException.class, () ->
            new FinancialRecord(STORE_ID, ORDER_ID, BigDecimal.TEN, null, null));
    }

    @Test
    void deveCancelarRegistro() {
        var record = new FinancialRecord(STORE_ID, ORDER_ID,
            BigDecimal.TEN, PaymentMethod.PIX, null);

        record.cancel();
        assertEquals(FinancialStatus.CANCELADO, record.getStatus());
    }

    @Test
    void deveEstornarRegistro() {
        var record = new FinancialRecord(STORE_ID, ORDER_ID,
            BigDecimal.TEN, PaymentMethod.PIX, null);

        record.refund();
        assertEquals(FinancialStatus.ESTORNADO, record.getStatus());
    }

    @Test
    void deveReconstituirComTodosOsCampos() {
        UUID id = UUID.randomUUID();
        var products = List.of(
            new FinancialRecordProduct(UUID.randomUUID(), "Suco", 1, new BigDecimal("8.00"))
        );

        var record = FinancialRecord.reconstitute(
            id, STORE_ID, ORDER_ID,
            java.time.LocalDate.of(2026, 8, 15),
            java.time.LocalTime.of(14, 30),
            new BigDecimal("50.00"), PaymentMethod.DINHEIRO,
            FinancialStatus.CONCLUIDO, products,
            java.time.LocalDateTime.of(2026, 8, 15, 14, 30));

        assertEquals(id, record.getId());
        assertEquals(STORE_ID, record.getStoreId());
        assertEquals(ORDER_ID, record.getOrderId());
        assertEquals(java.time.LocalDate.of(2026, 8, 15), record.getRecordDate());
        assertEquals(FinancialStatus.CONCLUIDO, record.getStatus());
        assertEquals(1, record.getProducts().size());
    }

    @Test
    void deveImplementarStoreScoped() {
        var record = new FinancialRecord(STORE_ID, ORDER_ID,
            BigDecimal.TEN, PaymentMethod.PIX, null);

        assertEquals(STORE_ID, record.getStoreId());
    }

    @Test
    void deveCriarFinancialRecordProductComValidacao() {
        var product = new FinancialRecordProduct(UUID.randomUUID(), "X-Bacon", 2, new BigDecimal("35.00"));
        assertEquals("X-Bacon", product.getProductName());
        assertEquals(2, product.getQuantity());
        assertEquals(new BigDecimal("35.00"), product.getPrice());
    }

    @Test
    void deveLancarExcecaoQuandoProductNameVazio() {
        assertThrows(IllegalArgumentException.class, () ->
            new FinancialRecordProduct(UUID.randomUUID(), "", 1, BigDecimal.TEN));
    }

    @Test
    void deveLancarExcecaoQuandoQuantidadeZero() {
        assertThrows(IllegalArgumentException.class, () ->
            new FinancialRecordProduct(UUID.randomUUID(), "X-Bacon", 0, BigDecimal.TEN));
    }
}
