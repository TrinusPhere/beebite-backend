package com.fidecode.biteflow.domain.catalog;

import com.fidecode.biteflow.domain.catalog.entity.ModifierGroup;
import com.fidecode.biteflow.domain.catalog.entity.ModifierOption;
import com.fidecode.biteflow.domain.catalog.valueobject.ModifierType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ModifierGroupTest {

    private final UUID storeId = UUID.randomUUID();

    @Test
    void shouldCreateRadioGroup() {
        ModifierGroup group = new ModifierGroup(storeId, "Ponto da Carne", ModifierType.RADIO, true, 1, 1, "🥩");
        assertEquals("Ponto da Carne", group.getName());
        assertEquals(ModifierType.RADIO, group.getType());
        assertTrue(group.isRequired());
        assertEquals(1, group.getMinSelections());
        assertEquals(1, group.getMaxSelections());
    }

    @Test
    void shouldCreateCheckboxGroup() {
        ModifierGroup group = new ModifierGroup(storeId, "Extras", ModifierType.CHECKBOX, false, 0, 3, "➕");
        assertEquals(ModifierType.CHECKBOX, group.getType());
        assertFalse(group.isRequired());
    }

    @Test
    void shouldRequireName() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModifierGroup(storeId, null, ModifierType.RADIO, false, 0, 1, null));
    }

    @Test
    void shouldRequireStoreId() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModifierGroup(null, "Test", ModifierType.RADIO, false, 0, 1, null));
    }

    @Test
    void shouldValidateMinMaxSelections() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModifierGroup(storeId, "Test", ModifierType.RADIO, false, 3, 1, null));
    }
}
