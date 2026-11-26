package com.trinusphere.beebite.application.sync;

import com.trinusphere.beebite.application.sync.port.EdgeStoreBootstrapPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BootstrapEdgeStoreUseCaseTest {

    @Mock
    EdgeStoreBootstrapPort bootstrapPort;

    private static final UUID SEED_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CLIENT_STORE_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID SHARED_CATALOG_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    @Test
    void shouldDoNothingWhenSyncModeIsCloud() {
        BootstrapEdgeStoreUseCase useCase = new BootstrapEdgeStoreUseCase(
                bootstrapPort,
                "CLOUD",
                Optional.of(CLIENT_STORE_ID.toString()),
                Optional.of(SHARED_CATALOG_ID.toString()),
                Optional.of("Chefs Burguer - Balcão"),
                Optional.of("chefs-burguer-balcao"),
                Optional.of("Rua Central, 100"),
                Optional.of("admin")
        );

        boolean ran = useCase.execute();

        assertFalse(ran);
        verifyNoInteractions(bootstrapPort);
    }

    @Test
    void shouldDoNothingWhenEdgeStoreIdIsMissing() {
        BootstrapEdgeStoreUseCase useCase = new BootstrapEdgeStoreUseCase(
                bootstrapPort,
                "EDGE",
                Optional.empty(),
                Optional.empty(),
                Optional.of("Chefs Burguer - Balcão"),
                Optional.empty(),
                Optional.empty(),
                Optional.of("admin")
        );

        boolean ran = useCase.execute();

        assertFalse(ran);
        verifyNoInteractions(bootstrapPort);
    }

    @Test
    void shouldBootstrapClientStoreDefaultingCatalogIdToStoreIdWhenCatalogEnvMissing() {
        BootstrapEdgeStoreUseCase useCase = new BootstrapEdgeStoreUseCase(
                bootstrapPort,
                "EDGE",
                Optional.of(CLIENT_STORE_ID.toString()),
                Optional.empty(),
                Optional.of("Chef's Burguer - Balcão"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        boolean ran = useCase.execute();

        assertTrue(ran);
        verify(bootstrapPort).bootstrapClientStoreAndCleanSeed(
                CLIENT_STORE_ID,
                CLIENT_STORE_ID,
                "Chef's Burguer - Balcão",
                "chef-s-burguer-balcao",
                "Unidade Presencial / Balcão",
                "admin",
                SEED_STORE_ID
        );
        verify(bootstrapPort).ensureLocalAdminIsStoreAdmin(
                CLIENT_STORE_ID,
                "admin",
                "Administrador - Chef's Burguer - Balcão"
        );
    }

    @Test
    void shouldUseExplicitSharedCatalogIdSlugAddressAndAdminLoginWhenProvided() {
        BootstrapEdgeStoreUseCase useCase = new BootstrapEdgeStoreUseCase(
                bootstrapPort,
                "EDGE",
                Optional.of(CLIENT_STORE_ID.toString()),
                Optional.of(SHARED_CATALOG_ID.toString()),
                Optional.of("Chefs Burguer - Loja 2"),
                Optional.of("chefs-loja-2"),
                Optional.of("Av Brasil, 500"),
                Optional.of("chefsburguer")
        );

        boolean ran = useCase.execute();

        assertTrue(ran);
        verify(bootstrapPort).bootstrapClientStoreAndCleanSeed(
                CLIENT_STORE_ID,
                SHARED_CATALOG_ID,
                "Chefs Burguer - Loja 2",
                "chefs-loja-2",
                "Av Brasil, 500",
                "chefsburguer",
                SEED_STORE_ID
        );
        verify(bootstrapPort).ensureLocalAdminIsStoreAdmin(
                CLIENT_STORE_ID,
                "chefsburguer",
                "Administrador - Chefs Burguer - Loja 2"
        );
    }

    @Test
    void shouldReturnFalseWhenEdgeStoreIdIsInvalidUuid() {
        BootstrapEdgeStoreUseCase useCase = new BootstrapEdgeStoreUseCase(
                bootstrapPort,
                "EDGE",
                Optional.of("not-a-uuid"),
                Optional.empty(),
                Optional.of("Loja Teste"),
                Optional.empty(),
                Optional.empty(),
                Optional.of("admin")
        );

        boolean ran = useCase.execute();

        assertFalse(ran);
        verifyNoInteractions(bootstrapPort);
    }
}
