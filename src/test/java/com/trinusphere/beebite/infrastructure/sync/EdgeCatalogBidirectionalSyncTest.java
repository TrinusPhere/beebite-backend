package com.trinusphere.beebite.infrastructure.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trinusphere.beebite.application.auth.port.PasswordEncoder;
import com.trinusphere.beebite.application.catalog.port.CatalogEventPublisher;
import com.trinusphere.beebite.application.sync.GetEdgeCatalogDeltaUseCase;
import com.trinusphere.beebite.application.sync.PullEdgeCatalogDeltaUseCase;
import com.trinusphere.beebite.application.sync.dto.EdgeCatalogDeltaResponse;
import com.trinusphere.beebite.application.sync.port.EdgeCatalogSyncApplier;
import com.trinusphere.beebite.application.sync.port.EdgeCloudSyncClient;
import com.trinusphere.beebite.domain.catalog.entity.Ingredient;
import com.trinusphere.beebite.domain.catalog.entity.ModifierGroup;
import com.trinusphere.beebite.domain.catalog.entity.ModifierOption;
import com.trinusphere.beebite.domain.catalog.entity.Product;
import com.trinusphere.beebite.domain.catalog.entity.ProductCategory;
import com.trinusphere.beebite.domain.catalog.valueobject.ChannelPrice;
import com.trinusphere.beebite.domain.catalog.valueobject.ModifierGroupSemanticType;
import com.trinusphere.beebite.domain.catalog.valueobject.ModifierType;
import com.trinusphere.beebite.domain.catalog.valueobject.Unit;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import com.trinusphere.beebite.domain.sync.exception.InvalidEdgeDeviceTokenException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EdgeCatalogBidirectionalSyncTest {

    @Mock
    EntityManager entityManager;
    @Mock
    Query nativeQuery;
    @Mock
    CatalogEventPublisher catalogEventPublisher;
    @Mock
    StoreRepository storeRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    EdgeCatalogSyncApplier edgeCatalogSyncApplier;
    @Mock
    EdgeCloudSyncClient edgeCloudSyncClient;

    private ObjectMapper objectMapper;
    private EdgeSyncPayloadSerializer serializer;
    private PanacheEdgeSnapshotApplier snapshotApplier;

    private static final UUID EDGE_STORE_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID CATALOG_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        serializer = new EdgeSyncPayloadSerializer(objectMapper);
        snapshotApplier = new PanacheEdgeSnapshotApplier(entityManager, objectMapper, catalogEventPublisher);
    }

    @Test
    void shouldApplyCatalogEventsFromEdgeOutboxToCloudCatalogAndNotifyIfood() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of(CATALOG_ID));
        when(nativeQuery.executeUpdate()).thenReturn(1);

        ProductCategory cat = ProductCategory.create(EDGE_STORE_ID, "Hambúrgueres", 1, List.of());
        cat.assignCatalog(CATALOG_ID);
        assertDoesNotThrow(() -> snapshotApplier.apply(
                EDGE_STORE_ID,
                "CATALOG_CATEGORY_UPSERT",
                cat.getId(),
                serializer.serializeCatalogCategory(cat, Instant.now(), null)
        ));

        Ingredient ing = new Ingredient(EDGE_STORE_ID, "Pão Brioche", new BigDecimal("50"), Unit.UN, new BigDecimal("1.80"), new BigDecimal("10"));
        ing.setId(UUID.randomUUID());
        ing.assignCatalog(CATALOG_ID);
        assertDoesNotThrow(() -> snapshotApplier.apply(
                EDGE_STORE_ID,
                "CATALOG_INGREDIENT_UPSERT",
                ing.getId(),
                serializer.serializeCatalogIngredient(ing, Instant.now(), null)
        ));

        ModifierGroup modGroup = new ModifierGroup(
                EDGE_STORE_ID, "Ponto da Carne", ModifierType.RADIO, true, 1, 1, null, ModifierGroupSemanticType.SPECIFICATION);
        modGroup.setId(UUID.randomUUID());
        modGroup.assignCatalog(CATALOG_ID);
        ModifierOption opt = new ModifierOption(modGroup.getId(), "Ao Ponto", BigDecimal.ZERO, 0);
        opt.setId(UUID.randomUUID());
        modGroup.replaceOptions(List.of(opt));
        assertDoesNotThrow(() -> snapshotApplier.apply(
                EDGE_STORE_ID,
                "CATALOG_MODIFIER_GROUP_UPSERT",
                modGroup.getId(),
                serializer.serializeCatalogModifierGroup(modGroup, Instant.now(), null)
        ));

        UUID productId = UUID.randomUUID();
        Product prod = Product.forCatalog(CATALOG_ID, EDGE_STORE_ID, "X-Bacon Duplo", cat.getId(), new BigDecimal("29.00"));
        prod.setId(productId);
        prod.initializeStock(15);
        prod.replaceChannelPrices(List.of(new ChannelPrice("IFOOD", new BigDecimal("35.90"))));

        snapshotApplier.apply(
                EDGE_STORE_ID,
                "CATALOG_PRODUCT_UPSERT",
                productId,
                serializer.serializeCatalogProduct(prod, Instant.now(), null)
        );

        verify(catalogEventPublisher).publishCatalogChanged(
                eq(CATALOG_ID),
                eq(EDGE_STORE_ID),
                eq(productId),
                eq("PRODUCT_UPDATED"),
                eq(new BigDecimal("35.90")),
                eq("AVAILABLE")
        );
    }

    @Test
    void shouldValidateDeviceTokenBeforeReturningCatalogDeltaOnCloud() {
        GetEdgeCatalogDeltaUseCase getDeltaUseCase = new GetEdgeCatalogDeltaUseCase(
                storeRepository, passwordEncoder, edgeCatalogSyncApplier);

        Store edgeStore = Store.reconstitute(
                EDGE_STORE_ID, "Chefs - Balcão", null, true, null, null, "chefs-balcao",
                "EDGE", "bf_edge_valid_secret", null, UUID.randomUUID(), null, true, CATALOG_ID, null);

        when(storeRepository.findById(EDGE_STORE_ID)).thenReturn(Optional.of(edgeStore));

        assertThrows(InvalidEdgeDeviceTokenException.class,
                () -> getDeltaUseCase.execute("wrong_token", EDGE_STORE_ID, null));

        Instant serverNow = Instant.parse("2026-11-25T18:00:00Z");
        EdgeCatalogDeltaResponse expectedDelta = new EdgeCatalogDeltaResponse(
                CATALOG_ID, "Cardápio Principal", serverNow,
                List.of(), List.of(), List.of(), List.of(), List.of()
        );
        when(edgeCatalogSyncApplier.buildDeltaForStore(EDGE_STORE_ID, null)).thenReturn(expectedDelta);

        EdgeCatalogDeltaResponse actual = getDeltaUseCase.execute("bf_edge_valid_secret", EDGE_STORE_ID, null);
        assertEquals(CATALOG_ID, actual.catalogId());
        assertEquals(serverNow, actual.serverTimestamp());
    }

    @Test
    void shouldPullCatalogDeltaFromCloudAndUpdateCursorInEdgeMode() {
        Instant previousPull = Instant.parse("2026-11-25T17:00:00Z");
        Instant newServerTs = Instant.parse("2026-11-25T17:15:00Z");

        Store localEdgeStore = Store.reconstitute(
                EDGE_STORE_ID, "Chefs - Balcão", null, true, null, null, "chefs-balcao",
                "EDGE", null, null, UUID.randomUUID(), null, true, CATALOG_ID, previousPull);

        when(storeRepository.findById(EDGE_STORE_ID)).thenReturn(Optional.of(localEdgeStore));
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

        EdgeCatalogDeltaResponse delta = new EdgeCatalogDeltaResponse(
                CATALOG_ID, "Cardápio Principal", newServerTs,
                List.of(), List.of(), List.of(), List.of(), List.of()
        );
        when(edgeCloudSyncClient.pullCatalogDelta(EDGE_STORE_ID, previousPull, "bf_edge_token"))
                .thenReturn(delta);
        when(edgeCatalogSyncApplier.applyDeltaLocally(EDGE_STORE_ID, delta)).thenReturn(4);

        PullEdgeCatalogDeltaUseCase pullUseCase = new PullEdgeCatalogDeltaUseCase(
                storeRepository,
                edgeCloudSyncClient,
                edgeCatalogSyncApplier,
                "EDGE",
                Optional.of(EDGE_STORE_ID.toString()),
                "bf_edge_token"
        );

        int applied = pullUseCase.execute();
        assertEquals(4, applied);
        assertEquals(newServerTs, localEdgeStore.getLastCatalogPullAt());
        verify(storeRepository).save(localEdgeStore);
    }
}

