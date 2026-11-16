package com.trinusphere.beebite.domain.catalog;

import com.trinusphere.beebite.domain.catalog.entity.Product;
import com.trinusphere.beebite.domain.catalog.entity.TechnicalSheetItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private final UUID storeId = UUID.randomUUID();

    @Test
    void shouldCreateProduct() {
        UUID categoryId = UUID.randomUUID();
        Product product = new Product(storeId, "X-Bacon", categoryId, BigDecimal.valueOf(35.00));
        assertEquals("X-Bacon", product.getName());
        assertEquals(categoryId, product.getCategoryId());
        assertEquals(storeId, product.getStoreId());
        assertTrue(product.getStatus() == com.trinusphere.beebite.domain.catalog.valueobject.ProductStatus.AVAILABLE);
        assertEquals(0, BigDecimal.valueOf(35.00).compareTo(product.getPrice()));
    }

    @Test
    void shouldRequireName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(storeId, null, UUID.randomUUID(), BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,
                () -> new Product(storeId, "  ", UUID.randomUUID(), BigDecimal.TEN));
    }

    @Test
    void shouldRequireStoreId() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(null, "X-Bacon", UUID.randomUUID(), BigDecimal.TEN));
    }

    @Test
    void shouldCalculateTotalCost() {
        Product product = new Product(storeId, "X-Bacon", UUID.randomUUID(), BigDecimal.valueOf(35.00));
        TechnicalSheetItem item1 = new TechnicalSheetItem(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(1),
                BigDecimal.valueOf(1.50));
        TechnicalSheetItem item2 = new TechnicalSheetItem(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(2),
                BigDecimal.valueOf(3.00));
        product.replaceTechnicalSheet(List.of(item1, item2));
        // totalCost = (1 * 1.50) + (2 * 3.00) = 1.50 + 6.00 = 7.50
        assertEquals(0, BigDecimal.valueOf(7.50).compareTo(product.calculateTotalCost()));
    }

    @Test
    void shouldCalculateProfit() {
        Product product = new Product(storeId, "X-Bacon", UUID.randomUUID(), BigDecimal.valueOf(35.00));
        TechnicalSheetItem item = new TechnicalSheetItem(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(1),
                BigDecimal.valueOf(8.40));
        product.replaceTechnicalSheet(List.of(item));
        // profit = 35.00 - 8.40 = 26.60
        assertEquals(0, BigDecimal.valueOf(26.60).compareTo(product.calculateProfit()));
    }

    @Test
    void shouldReturnZeroCostWithEmptySheet() {
        Product product = new Product(storeId, "X-Bacon", UUID.randomUUID(), BigDecimal.valueOf(35.00));
        assertEquals(0, BigDecimal.ZERO.compareTo(product.calculateTotalCost()));
        assertEquals(0, BigDecimal.valueOf(35.00).compareTo(product.calculateProfit()));
    }
}
