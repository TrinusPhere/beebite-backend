package com.fidecode.biteflow.application;

import com.fidecode.biteflow.application.store.GetStoreInfoUseCase;
import com.fidecode.biteflow.application.store.UpdateStoreInfoUseCase;
import com.fidecode.biteflow.application.store.dto.StoreInfoRequest;
import com.fidecode.biteflow.application.store.dto.StoreInfoResponse;
import com.fidecode.biteflow.domain.store.entity.Store;
import com.fidecode.biteflow.domain.store.exception.StoreNotFoundException;
import com.fidecode.biteflow.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreUseCaseTest {

    @Mock StoreRepository storeRepository;

    @InjectMocks GetStoreInfoUseCase getStoreInfoUseCase;

    private final UUID storeId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private Store existingStore;

    @BeforeEach
    void setUp() {
        existingStore = new Store("BiteFlow");
        existingStore.setId(storeId);
        existingStore.setAddress("Rua A, 100");
        existingStore.setOpen(true);
    }

    @Test
    void shouldReturnStoreInfoResponse() {
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(existingStore));

        StoreInfoResponse response = getStoreInfoUseCase.execute(storeId);

        assertEquals("BiteFlow", response.name());
        assertTrue(response.isOpen());
        assertEquals("Rua A, 100", response.address());
    }

    @Test
    void shouldThrowWhenStoreNotFound() {
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class, () -> getStoreInfoUseCase.execute(storeId));
    }

    // ── UpdateStoreInfo tests ─────────────────────────

    @Test
    void shouldUpdateStoreAndReturnResponse() {
        UpdateStoreInfoUseCase updateStoreInfoUseCase = new UpdateStoreInfoUseCase(storeRepository);

        StoreInfoRequest request = new StoreInfoRequest("BiteFlow Premium", "https://logo.png", true, "10:00-22:00", "Rua B, 200");
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(existingStore));
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

        StoreInfoResponse response = updateStoreInfoUseCase.execute(storeId, request);

        assertEquals("BiteFlow Premium", response.name());
        assertTrue(response.isOpen());
        verify(storeRepository).save(any(Store.class));
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentStore() {
        UpdateStoreInfoUseCase updateStoreInfoUseCase = new UpdateStoreInfoUseCase(storeRepository);

        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class,
            () -> updateStoreInfoUseCase.execute(storeId,
                new StoreInfoRequest("Nome", null, false, null, null)));
    }
}
