package com.fidecode.biteflow.domain;

import com.fidecode.biteflow.domain.store.entity.Store;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoreTest {

    @Test
    void shouldCreateStoreWithRequiredFields() {
        Store store = new Store("BiteFlow Lanchonete");
        assertEquals("BiteFlow Lanchonete", store.getName());
    }

    @Test
    void shouldHaveIsOpenFalseByDefault() {
        Store store = new Store("BiteFlow Lanchonete");
        assertFalse(store.isOpen());
    }

    @Test
    void shouldAllowUpdatingFields() {
        Store store = new Store("BiteFlow");
        store.setName("BiteFlow Premium");
        store.setOpen(true);
        store.setAddress("Rua A, 100");
        store.setOpeningHours("10:00 - 22:00");
        store.setLogoUrl("https://logo.png");

        assertEquals("BiteFlow Premium", store.getName());
        assertTrue(store.isOpen());
        assertEquals("Rua A, 100", store.getAddress());
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(IllegalArgumentException.class, () -> new Store(null));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Store("  "));
    }
}
