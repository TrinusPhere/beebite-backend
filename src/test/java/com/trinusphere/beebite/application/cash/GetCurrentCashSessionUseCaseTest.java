package com.trinusphere.beebite.application.cash;

import com.trinusphere.beebite.application.cash.dto.CashSessionResponse;
import com.trinusphere.beebite.domain.cash.entity.CashSession;
import com.trinusphere.beebite.domain.cash.exception.NoOpenSessionException;
import com.trinusphere.beebite.domain.cash.repository.CashSessionRepository;
import com.trinusphere.beebite.domain.cash.valueobject.CashSessionStatus;
import com.trinusphere.beebite.domain.store.entity.Store;
import com.trinusphere.beebite.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCurrentCashSessionUseCaseTest {

    @Mock
    CashSessionRepository sessionRepository;

    @Mock
    StoreRepository storeRepository;

    private static final UUID STORE_ID = UUID.fromString("e72810a1-afe4-43f1-8dc9-71054a010216");
    private static final UUID EDGE_OPERATOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CLOUD_OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID REGISTER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SESSION_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private CashSession buildOpenSession(UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        return CashSession.reconstitute(
                SESSION_ID,
                STORE_ID,
                REGISTER_ID,
                userId,
                CashSessionStatus.OPEN,
                now,
                null,
                new BigDecimal("150.00"),
                null,
                null,
                null,
                null,
                now,
                now
        );
    }

    @Test
    void shouldReturnOwnOpenSessionWhenPresent() {
        GetCurrentCashSessionUseCase useCase = new GetCurrentCashSessionUseCase(
                sessionRepository, storeRepository, "CLOUD");

        CashSession ownSession = buildOpenSession(CLOUD_OWNER_ID);
        when(sessionRepository.findOpenSessionByUserId(STORE_ID, CLOUD_OWNER_ID))
                .thenReturn(Optional.of(ownSession));

        CashSessionResponse response = useCase.execute(STORE_ID, CLOUD_OWNER_ID);

        assertEquals(SESSION_ID.toString(), response.id());
        assertEquals("OPEN", response.status());
        verifyNoInteractions(storeRepository);
    }

    @Test
    void shouldFallbackToStoreOpenSessionOnCloudWhenViewingEdgeStore() {
        GetCurrentCashSessionUseCase useCase = new GetCurrentCashSessionUseCase(
                sessionRepository, storeRepository, "CLOUD");

        Store edgeStore = Store.reconstitute(
                STORE_ID, "Chefs Burguer - Balcão", null, true, null,
                "Rua Central, 100", "chefs-balcao", "EDGE", "bf_edge_token", null);
        CashSession edgeSession = buildOpenSession(EDGE_OPERATOR_ID);

        when(sessionRepository.findOpenSessionByUserId(STORE_ID, CLOUD_OWNER_ID))
                .thenReturn(Optional.empty());
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(edgeStore));
        when(sessionRepository.findByStoreIdAndStatus(STORE_ID, CashSessionStatus.OPEN))
                .thenReturn(List.of(edgeSession));

        CashSessionResponse response = useCase.execute(STORE_ID, CLOUD_OWNER_ID);

        assertEquals(SESSION_ID.toString(), response.id());
        assertEquals( new BigDecimal("150.00"), response.initialAmount());
    }

    @Test
    void shouldNotFallbackToAnotherOperatorsSessionWhenRunningLocallyOnEdgeNode() {
        GetCurrentCashSessionUseCase useCase = new GetCurrentCashSessionUseCase(
                sessionRepository, storeRepository, "EDGE");

        when(sessionRepository.findOpenSessionByUserId(STORE_ID, CLOUD_OWNER_ID))
                .thenReturn(Optional.empty());

        assertThrows(NoOpenSessionException.class,
                () -> useCase.execute(STORE_ID, CLOUD_OWNER_ID));
        verifyNoInteractions(storeRepository);
        verify(sessionRepository, never()).findByStoreIdAndStatus(any(), any());
    }

    @Test
    void shouldNotFallbackToAnotherOperatorsSessionOnCloudNativeStore() {
        GetCurrentCashSessionUseCase useCase = new GetCurrentCashSessionUseCase(
                sessionRepository, storeRepository, "CLOUD");

        Store cloudStore = Store.reconstitute(
                STORE_ID, "Loja Cloud", null, true, null,
                "Rua A, 10", "loja-cloud", "CLOUD", null, null);

        when(sessionRepository.findOpenSessionByUserId(STORE_ID, CLOUD_OWNER_ID))
                .thenReturn(Optional.empty());
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(cloudStore));

        assertThrows(NoOpenSessionException.class,
                () -> useCase.execute(STORE_ID, CLOUD_OWNER_ID));
        verify(sessionRepository, never()).findByStoreIdAndStatus(any(), any());
    }
}

