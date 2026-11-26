package com.trinusphere.beebite.domain.catalog;

import com.trinusphere.beebite.domain.catalog.entity.Catalog;
import com.trinusphere.beebite.domain.catalog.entity.Product;
import com.trinusphere.beebite.domain.catalog.valueobject.ProductStatus;
import com.trinusphere.beebite.domain.shared.CatalogGuard;
import com.trinusphere.beebite.domain.shared.exception.ForbiddenAccessException;
import com.trinusphere.beebite.domain.store.entity.Store;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CatalogDomainTest {

    @Test
    void shouldCreateCatalogForTenantAndValidateInvariants() {
        UUID tenantId = UUID.randomUUID();
        Catalog catalog = new Catalog(tenantId, "Cardápio Principal - Chefs Burguer", true);

        assertEquals(tenantId, catalog.getTenantId());
        assertEquals("Cardápio Principal - Chefs Burguer", catalog.getName());
        assertTrue(catalog.isDefaultCatalog());
        assertNotNull(catalog.getCreatedAt());
        assertNotNull(catalog.getUpdatedAt());

        catalog.rename("Cardápio Hamburgueria");
        assertEquals("Cardápio Hamburgueria", catalog.getName());

        catalog.unmarkDefault();
        assertFalse(catalog.isDefaultCatalog());
        catalog.markAsDefault();
        assertTrue(catalog.isDefaultCatalog());

        assertThrows(IllegalArgumentException.class, () -> new Catalog(null, "Cardápio", true));
        assertThrows(IllegalArgumentException.class, () -> new Catalog(tenantId, "  ", true));
        assertThrows(IllegalArgumentException.class, () -> catalog.rename(""));
    }

    @Test
    void shouldEnforceCatalogGuardIsolation() {
        UUID catalogA = UUID.randomUUID();
        UUID catalogB = UUID.randomUUID();
        UUID storeA = UUID.randomUUID();

        Product product = Product.forCatalog(catalogA, storeA, "X-Bacon", null, new BigDecimal("28.00"));

        assertSame(product, CatalogGuard.enforce(product, catalogA));
        assertThrows(ForbiddenAccessException.class, () -> CatalogGuard.enforce(product, catalogB));
    }

    @Test
    void shouldComputeEffectiveProductStatusCombiningGlobalAndLocalStoreStatus() {
        UUID catalogId = UUID.randomUUID();
        UUID storeEdgeId = UUID.randomUUID();

        Product product = Product.forCatalog(catalogId, storeEdgeId, "Coca-Cola Lata", null, new BigDecimal("7.00"));
        assertEquals(ProductStatus.AVAILABLE, product.getGlobalStatus());
        assertEquals(ProductStatus.AVAILABLE, product.getLocalStatus());
        assertEquals(ProductStatus.AVAILABLE, product.getEffectiveStatus());

        // Pausa apenas local na unidade (ex: acabou no freezer do balcão, mas delivery/iFood seguem ativos)
        product.configureLocalStoreStatus(ProductStatus.UNAVAILABLE);
        assertEquals(ProductStatus.AVAILABLE, product.getGlobalStatus());
        assertEquals(ProductStatus.UNAVAILABLE, product.getLocalStatus());
        assertEquals(ProductStatus.UNAVAILABLE, product.getEffectiveStatus());

        // Reativa localmente, mas pausa globalmente (cardápio inteiro + iFood)
        product.configureLocalStoreStatus(ProductStatus.AVAILABLE);
        product.configureAvailability(ProductStatus.UNAVAILABLE, true, true);
        assertEquals(ProductStatus.UNAVAILABLE, product.getGlobalStatus());
        assertEquals(ProductStatus.AVAILABLE, product.getLocalStatus());
        assertEquals(ProductStatus.UNAVAILABLE, product.getEffectiveStatus());
    }

    @Test
    void shouldAssignCatalogAndTrackCatalogPullTimestampOnStore() {
        UUID catalogId = UUID.randomUUID();
        Instant pulledAt = Instant.parse("2026-11-25T14:30:00Z");

        Store store = new Store("Chefs Burguer - Balcão");
        store.assignCatalog(catalogId);
        store.markCatalogPulled(pulledAt);

        assertEquals(catalogId, store.getCatalogId());
        assertEquals(pulledAt, store.getLastCatalogPullAt());
    }
}

