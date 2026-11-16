package com.trinusphere.beebite.domain.catalog;

import com.trinusphere.beebite.domain.catalog.entity.ProductComboComponent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductComboComponent — Regras de Domínio")
class ProductComboComponentTest {

    @Test
    @DisplayName("Deve criar componente de combo válido")
    void shouldCreateComboComponent() {
        UUID storeId = UUID.randomUUID();
        UUID comboId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();

        ProductComboComponent comp = new ProductComboComponent(storeId, comboId, componentId, 2);

        assertNotNull(comp);
        assertEquals(storeId, comp.getStoreId());
        assertEquals(comboId, comp.getComboProductId());
        assertEquals(componentId, comp.getComponentProductId());
        assertEquals(2, comp.getQuantity());
        assertNotNull(comp.getCreatedAt());
        assertNull(comp.getDeletedAt());
    }

    @Test
    @DisplayName("Deve validar quantidade maior que zero")
    void shouldValidatePositiveQuantity() {
        UUID storeId = UUID.randomUUID();
        UUID comboId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> new ProductComboComponent(storeId, comboId, componentId, 0));
        assertThrows(IllegalArgumentException.class, () -> new ProductComboComponent(storeId, comboId, componentId, -1));
    }
}

