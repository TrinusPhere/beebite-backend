package com.fidecode.biteflow.domain.order;

import com.fidecode.biteflow.domain.order.entity.OrderItem;
import com.fidecode.biteflow.domain.order.entity.OrderItemModifier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    void deveCalcularTotalPriceSemModificadores() {
        OrderItem item = new OrderItem(UUID.randomUUID(), "X-Bacon", 2, new BigDecimal("25.00"), "Capricha no molho");

        // totalPrice = (25 + 0) * 2 = 50
        assertEquals(new BigDecimal("50.00"), item.getTotalPrice());
        assertEquals(BigDecimal.ZERO, item.getExtraPrice());
        assertEquals("Capricha no molho", item.getObservation());
    }

    @Test
    void deveCalcularTotalPriceComModificadores() {
        OrderItem item = new OrderItem(UUID.randomUUID(), "X-Bacon", 2, new BigDecimal("25.00"), null);

        item.addModifier(new OrderItemModifier(UUID.randomUUID(), "Adicionais", UUID.randomUUID(), "Bacon Extra",
                new BigDecimal("5.00")));
        item.addModifier(new OrderItemModifier(UUID.randomUUID(), "Adicionais", UUID.randomUUID(), "Queijo",
                new BigDecimal("3.00")));

        // extraPrice = 5 + 3 = 8
        // totalPrice = (25 + 8) * 2 = 66
        assertEquals(new BigDecimal("8.00"), item.getExtraPrice());
        assertEquals(new BigDecimal("66.00"), item.getTotalPrice());
    }

    @Test
    void deveLancarExcecaoSeProductNameVazio() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(UUID.randomUUID(), "", 1, new BigDecimal("10.00"), null));
    }

    @Test
    void deveLancarExcecaoSeProductNameNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(UUID.randomUUID(), null, 1, new BigDecimal("10.00"), null));
    }

    @Test
    void deveLancarExcecaoSeQuantidadeZero() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(UUID.randomUUID(), "X-Bacon", 0, new BigDecimal("10.00"), null));
    }

    @Test
    void deveLancarExcecaoSeQuantidadeNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(UUID.randomUUID(), "X-Bacon", -1, new BigDecimal("10.00"), null));
    }

    @Test
    void deveLancarExcecaoSeUnitPriceNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(UUID.randomUUID(), "X-Bacon", 1, new BigDecimal("-5.00"), null));
    }

    @Test
    void deveAdicionarRemocao() {
        OrderItem item = new OrderItem(UUID.randomUUID(), "X-Bacon", 1, new BigDecimal("25.00"), null);
        var removal = new com.fidecode.biteflow.domain.order.entity.OrderItemRemoval(UUID.randomUUID(), "Alface");
        item.addRemoval(removal);

        assertEquals(1, item.getRemovals().size());
        assertEquals("Alface", item.getRemovals().get(0).getIngredientName());
    }
}
