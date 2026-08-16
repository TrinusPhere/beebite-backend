package com.fidecode.biteflow.domain.catalog;

import com.fidecode.biteflow.domain.catalog.entity.Product;
import com.fidecode.biteflow.domain.catalog.entity.TechnicalSheetItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private final UUID storeId = UUID.randomUUID();

    @Test
    void shouldCreateProduct() {
        Product product = new Product(storeId, "X-Bacon", "Hambúrgueres", BigDecimal.valueOf(35.00));
        assertEquals("X-Bacon", product.getName());
        assertEquals("Hambúrgueres", product.getCategory());
        assertEquals(storeId, product.getStoreId());
        assertTrue(product.isActive());
        assertEquals(0, BigDecimal.valueOf(35.00).compareTo(product.getPrice()));
    }

    @Test
    void shouldRequireName() {
        assertThrows(IllegalArgumentException.class,
            () -> new Product(storeId, null, "cat", BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
            () -> new Product(storeId, "  ", "cat", BigDecimal.TEN));
    }

    @Test
    void shouldRequireStoreId() {
        assertThrows(IllegalArgumentException.class,
            () -> new Product(null, "X-Bacon", "cat", BigDecimal.TEN));
    }

    @Test
    void shouldCalculateTotalCost() {
        Product product = new Product(storeId, "X-Bacon", "Hambúrgueres", BigDecimal.valueOf(35.00));
        TechnicalSheetItem item1 = new TechnicalSheetItem(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(1), BigDecimal.valueOf(1.50));
        TechnicalSheetItem item2 = new TechnicalSheetItem(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(2), BigDecimal.valueOf(3.00));
        product.setTechnicalSheet(List.of(item1, item2));
        // totalCost = (1 * 1.50) + (2 * 3.00) = 1.50 + 6.00 = 7.50
        assertEquals(0, BigDecimal.valueOf(7.50).compareTo(product.calculateTotalCost()));
    }

    @Test
    void shouldCalculateProfit() {
        Product product = new Product(storeId, "X-Bacon", "Hambúrgueres", BigDecimal.valueOf(35.00));
        TechnicalSheetItem item = new TechnicalSheetItem(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(1), BigDecimal.valueOf(8.40));
        product.setTechnicalSheet(List.of(item));
        // profit = 35.00 - 8.40 = 26.60
        assertEquals(0, BigDecimal.valueOf(26.60).compareTo(product.calculateProfit()));
    }

    @Test
    void shouldReturnZeroCostWithEmptySheet() {
        Product product = new Product(storeId, "X-Bacon", "Hambúrgueres", BigDecimal.valueOf(35.00));
        assertEquals(0, BigDecimal.ZERO.compareTo(product.calculateTotalCost()));
        assertEquals(0, BigDecimal.valueOf(35.00).compareTo(product.calculateProfit()));
    }
}
