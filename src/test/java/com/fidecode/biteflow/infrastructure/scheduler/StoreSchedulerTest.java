package com.fidecode.biteflow.infrastructure.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fidecode.biteflow.domain.store.entity.Store;
import com.fidecode.biteflow.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreSchedulerTest {

    @Mock
    private StoreRepository storeRepository;

    private ObjectMapper objectMapper;
    private StoreScheduler scheduler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        scheduler = new StoreScheduler(storeRepository, objectMapper);
    }

    @Test
    void shouldGracefullyIgnoreLegacyPlainTextOpeningHours() {
        Store store = Store.reconstitute(
                UUID.randomUUID(),
                "Loja Teste",
                null,
                true,
                "10:00 - 22:00",
                "Rua Teste, 123",
                "loja-teste"
        );
        when(storeRepository.findAll()).thenReturn(List.of(store));

        assertDoesNotThrow(() -> scheduler.evaluateStoreSchedules());

        verify(storeRepository, never()).save(any());
    }

    @Test
    void shouldGracefullyHandleMalformedJsonWithoutFailing() {
        Store store = Store.reconstitute(
                UUID.randomUUID(),
                "Loja Teste",
                null,
                true,
                "{autoSchedule: true, bad_json",
                "Rua Teste, 123",
                "loja-teste"
        );
        when(storeRepository.findAll()).thenReturn(List.of(store));

        assertDoesNotThrow(() -> scheduler.evaluateStoreSchedules());

        verify(storeRepository, never()).save(any());
    }

    @Test
    void shouldDoNothingWhenOpeningHoursIsNull() {
        Store store = Store.reconstitute(
                UUID.randomUUID(),
                "Loja Teste",
                null,
                true,
                null,
                "Rua Teste, 123",
                "loja-teste"
        );
        when(storeRepository.findAll()).thenReturn(List.of(store));

        assertDoesNotThrow(() -> scheduler.evaluateStoreSchedules());

        verify(storeRepository, never()).save(any());
    }
}

