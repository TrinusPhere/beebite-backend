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
        store.updateInfo("BiteFlow Premium", "https://logo.png", true, "10:00 - 22:00", "Rua A, 100");

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

    @Test
    void shouldDefaultToCloudSyncModeAndSupportEdgeMetadata() {
        Store store = new Store("Chefs Burguer Balcão");
        assertEquals("CLOUD", store.getSyncMode());
        assertNull(store.getEdgeApiKeyHash());
        assertNull(store.getLastEdgeSyncAt());

        java.time.Instant syncTime = java.time.Instant.now();
        store.configureEdgeSync("EDGE", "hashed_key_123");
        store.markEdgeSynced(syncTime);

        assertEquals("EDGE", store.getSyncMode());
        assertEquals("hashed_key_123", store.getEdgeApiKeyHash());
        assertEquals(syncTime, store.getLastEdgeSyncAt());
    }
}
