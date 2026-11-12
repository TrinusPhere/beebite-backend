package com.fidecode.beebite.domain.catalog;

import com.fidecode.beebite.domain.catalog.entity.Additional;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdditionalTest {

    private final UUID storeId = UUID.randomUUID();

    @Test
    void shouldCreateAdditional() {
        Additional additional = new Additional(storeId, "Queijo Coalho", BigDecimal.valueOf(5.00), "Queijos");
        assertEquals("Queijo Coalho", additional.getName());
        assertEquals(storeId, additional.getStoreId());
        assertEquals(0, BigDecimal.valueOf(5.00).compareTo(additional.getPrice()));
        assertEquals("Queijos", additional.getCategory());
        assertTrue(additional.isActive());
    }

    @Test
    void shouldToggleActive() {
        Additional additional = new Additional(storeId, "Queijo", BigDecimal.valueOf(3.00), "Queijos");
        assertTrue(additional.isActive());
        additional.toggleActive();
        assertFalse(additional.isActive());
        additional.toggleActive();
        assertTrue(additional.isActive());
    }

    @Test
    void shouldRequireName() {
        assertThrows(IllegalArgumentException.class, () -> new Additional(storeId, null, BigDecimal.TEN, "cat"));
    }

    @Test
    void shouldRequireStoreId() {
        assertThrows(IllegalArgumentException.class, () -> new Additional(null, "Queijo", BigDecimal.TEN, "cat"));
    }
}
