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

    @Test
    void shouldDoNothingWhenSyncModeIsCloud() {
        BootstrapEdgeStoreUseCase useCase = new BootstrapEdgeStoreUseCase(
                bootstrapPort,
                "CLOUD",
                Optional.of(CLIENT_STORE_ID.toString()),
                "Chefs Burguer - Balcão",
                "chefs-burguer-balcao",
                "Rua Central, 100",
                "admin"
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
                "Chefs Burguer - Balcão",
                "",
                "",
                "admin"
        );

        boolean ran = useCase.execute();

        assertFalse(ran);
        verifyNoInteractions(bootstrapPort);
    }

    @Test
    void shouldBootstrapClientStoreAndDemoteSeedAdminWhenSyncModeIsEdge() {
        BootstrapEdgeStoreUseCase useCase = new BootstrapEdgeStoreUseCase(
                bootstrapPort,
                "EDGE",
                Optional.of(CLIENT_STORE_ID.toString()),
                "Chef's Burguer - Balcão",
                "",
                "",
                ""
        );

        boolean ran = useCase.execute();

        assertTrue(ran);
        verify(bootstrapPort).bootstrapClientStoreAndCleanSeed(
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
    void shouldUseExplicitSlugAddressAndAdminLoginWhenProvided() {
        BootstrapEdgeStoreUseCase useCase = new BootstrapEdgeStoreUseCase(
                bootstrapPort,
                "EDGE",
                Optional.of(CLIENT_STORE_ID.toString()),
                "Chefs Burguer - Loja 2",
                "chefs-loja-2",
                "Av Brasil, 500",
                "chefsburguer"
        );

        boolean ran = useCase.execute();

        assertTrue(ran);
        verify(bootstrapPort).bootstrapClientStoreAndCleanSeed(
                CLIENT_STORE_ID,
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
                "Loja Teste",
                "",
                "",
                "admin"
        );

        boolean ran = useCase.execute();

        assertFalse(ran);
        verifyNoInteractions(bootstrapPort);
    }
}
