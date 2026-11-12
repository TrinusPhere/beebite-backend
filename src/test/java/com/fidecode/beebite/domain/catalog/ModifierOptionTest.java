package com.fidecode.beebite.domain.catalog;

import com.fidecode.beebite.domain.catalog.entity.ModifierOption;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ModifierOptionTest {

    @Test
    void shouldCreateOption() {
        UUID groupId = UUID.randomUUID();
        ModifierOption option = new ModifierOption(groupId, "Ao Ponto", BigDecimal.ZERO, 1);
        assertEquals(groupId, option.getModifierGroupId());
        assertEquals("Ao Ponto", option.getName());
        assertEquals(0, BigDecimal.ZERO.compareTo(option.getPrice()));
        assertEquals(1, option.getSortOrder());
    }

    @Test
    void shouldAllowZeroPrice() {
        ModifierOption option = new ModifierOption(UUID.randomUUID(), "Mal Passado", BigDecimal.ZERO, 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(option.getPrice()));
    }

    @Test
    void shouldRequireName() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModifierOption(UUID.randomUUID(), null, BigDecimal.ZERO, 0));
    }

}
