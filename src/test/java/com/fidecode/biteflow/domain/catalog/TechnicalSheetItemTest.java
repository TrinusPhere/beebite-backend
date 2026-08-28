package com.fidecode.biteflow.domain.catalog;

import com.fidecode.biteflow.domain.catalog.entity.TechnicalSheetItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TechnicalSheetItemTest {

    @Test
    void shouldCreateWithCostSnapshot() {
        UUID productId = UUID.randomUUID();
        UUID ingredientId = UUID.randomUUID();
        TechnicalSheetItem item = new TechnicalSheetItem(productId, ingredientId, BigDecimal.valueOf(2),
                BigDecimal.valueOf(1.50));
        assertEquals(productId, item.getProductId());
        assertEquals(ingredientId, item.getIngredientId());
        assertEquals(0, BigDecimal.valueOf(2).compareTo(item.getQuantity()));
        assertEquals(0, BigDecimal.valueOf(1.50).compareTo(item.getCostSnapshot()));
    }

    @Test
    void shouldCalculateLineCost() {
        TechnicalSheetItem item = new TechnicalSheetItem(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(3),
                BigDecimal.valueOf(2.00));
        // lineCost = quantity * costSnapshot = 3 * 2.00 = 6.00
        assertEquals(0, BigDecimal.valueOf(6.00).compareTo(item.getLineCost()));
    }

    @Test
    void shouldRequireIngredientId() {
        assertThrows(IllegalArgumentException.class,
                () -> new TechnicalSheetItem(UUID.randomUUID(), null, BigDecimal.ONE, BigDecimal.ONE));
    }
}
