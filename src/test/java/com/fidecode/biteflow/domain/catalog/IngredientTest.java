package com.fidecode.biteflow.domain.catalog;

import com.fidecode.biteflow.domain.catalog.entity.Ingredient;
import com.fidecode.biteflow.domain.catalog.valueobject.Unit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IngredientTest {

    private final UUID storeId = UUID.randomUUID();

    @Test
    void shouldCreateIngredient() {
        Ingredient ingredient = new Ingredient(storeId, "Pão Brioche", BigDecimal.valueOf(50), Unit.UN,
                BigDecimal.valueOf(1.50), BigDecimal.valueOf(10));
        assertEquals("Pão Brioche", ingredient.getName());
        assertEquals(storeId, ingredient.getStoreId());
        assertEquals(Unit.UN, ingredient.getUnit());
        assertEquals(0, BigDecimal.valueOf(50).compareTo(ingredient.getStock()));
        assertEquals(0, BigDecimal.valueOf(1.50).compareTo(ingredient.getCostPrice()));
        assertEquals(0, BigDecimal.valueOf(10).compareTo(ingredient.getMinStock()));
    }

    @Test
    void shouldRequireName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Ingredient(storeId, null, BigDecimal.ZERO, Unit.UN, BigDecimal.ZERO, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> new Ingredient(storeId, "  ", BigDecimal.ZERO, Unit.UN, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void shouldRequireStoreId() {
        assertThrows(IllegalArgumentException.class,
                () -> new Ingredient(null, "Pão", BigDecimal.ZERO, Unit.UN, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void shouldDetectLowStock() {
        Ingredient ingredient = new Ingredient(storeId, "Pão", BigDecimal.valueOf(5), Unit.UN, BigDecimal.valueOf(1),
                BigDecimal.valueOf(10));
        assertTrue(ingredient.isLowStock());
    }

    @Test
    void shouldNotBeLowStockWhenAboveMinimum() {
        Ingredient ingredient = new Ingredient(storeId, "Pão", BigDecimal.valueOf(15), Unit.UN, BigDecimal.valueOf(1),
                BigDecimal.valueOf(10));
        assertFalse(ingredient.isLowStock());
    }

    @Test
    void shouldNotBeLowStockWhenAtMinimum() {
        Ingredient ingredient = new Ingredient(storeId, "Pão", BigDecimal.valueOf(10), Unit.UN, BigDecimal.valueOf(1),
                BigDecimal.valueOf(10));
        assertFalse(ingredient.isLowStock());
    }
}
